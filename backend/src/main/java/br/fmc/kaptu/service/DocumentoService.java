
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.StatusClassificacao;
import br.fmc.kaptu.domain.documento.TipoDocumento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.ocr.ClassificadorDeDocumento;
import br.fmc.kaptu.ocr.DocumentoFactory;
import br.fmc.kaptu.ocr.ExtratorDeCampos;
import br.fmc.kaptu.ocr.PdfBoxExtrator;
import br.fmc.kaptu.ocr.TesseractExtrator;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.DocumentoSpecifications;
import br.fmc.kaptu.repository.ImovelRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentoService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentoService.class);

    private final SupabaseStorageService supabaseStorageService;
    private final DocumentoRepository documentoRepository;
    private final ImovelRepository imovelRepository;
    private final DocumentoFactory documentoFactory;
    private final ClassificadorDeDocumento classificador;
    private final ExtratorDeCampos extratorDeCampos;
    private final PdfBoxExtrator pdfBoxExtrator;
    private final TesseractExtrator tesseractExtrator;
    private final Path pastaArquivos;

    public DocumentoService(
            DocumentoRepository documentoRepository,
            ImovelRepository imovelRepository,
            DocumentoFactory documentoFactory,
            ClassificadorDeDocumento classificador,
            ExtratorDeCampos extratorDeCampos,
            PdfBoxExtrator pdfBoxExtrator,
            TesseractExtrator tesseractExtrator,
            SupabaseStorageService supabaseStorageService,
            @Value("${kaptu.pasta-arquivos:./arquivos}")
            String pastaArquivos) {

        this.supabaseStorageService = supabaseStorageService;
        this.documentoRepository = documentoRepository;
        this.imovelRepository = imovelRepository;
        this.documentoFactory = documentoFactory;
        this.classificador = classificador;
        this.extratorDeCampos = extratorDeCampos;
        this.pdfBoxExtrator = pdfBoxExtrator;
        this.tesseractExtrator = tesseractExtrator;
        this.pastaArquivos = Path.of(pastaArquivos)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public Documento processarUploadComOCR(
            MultipartFile arquivo,
            TipoDocumento tipoInformado,
            Long imovelId) {

        validarArquivo(arquivo);

        Imovel imovel = resolverImovel(imovelId);

        Path destino = null;
        String caminhoEnviado = null;

        try {
            Files.createDirectories(pastaArquivos);

            String nomeOriginal = obterNomeSeguro(
                    arquivo.getOriginalFilename()
            );

            String extensao = obterExtensao(nomeOriginal);

            String nomeSalvo = UUID.randomUUID() + extensao;

            destino = pastaArquivos.resolve(nomeSalvo);

            arquivo.transferTo(destino.toFile());

            String texto = extrairTexto(destino);

            TipoDocumento tipo = tipoInformado != null
                    ? tipoInformado
                    : classificador.classificar(texto);

            Documento documento = documentoFactory.criar(tipo);

            documento.definirTitulo(nomeOriginal);
            documento.definirTextoExtraido(texto);
            documento.definirImovel(imovel);

            Map<String, String> campos =
                    extratorDeCampos.extrair(texto, tipo);

            documento.aplicarCamposExtraidos(campos);

            if (documento.getStatusClassificacao()
                    == StatusClassificacao.PENDENTE) {
                documento.marcarClassificado();
            }

            caminhoEnviado = nomeSalvo;

            supabaseStorageService.enviarArquivo(
                    destino,
                    caminhoEnviado
            );

            documento.definirCaminhoArquivo(caminhoEnviado);

            return documentoRepository.save(documento);

        } catch (RegraDeNegocioExcecao
                | RecursoNaoEncontradoExcecao e) {

            limparUpload(caminhoEnviado, e);
            throw e;

        } catch (IOException e) {

            limparUpload(caminhoEnviado, e);

            throw new IllegalStateException(
                    "Falha ao gravar o arquivo enviado.", e
            );

        } catch (RuntimeException e) {

            limparUpload(caminhoEnviado, e);

            log.error(
                    "Erro inesperado ao processar documento {}",
                    arquivo.getOriginalFilename(),
                    e
            );

            throw e;

        } finally {

            if (destino != null) {
                try {
                    Files.deleteIfExists(destino);
                } catch (IOException erroExclusao) {
                    log.warn(
                            "Não foi possível excluir arquivo temporário: {}",
                            erroExclusao.getMessage()
                    );
                }
            }
        }
    }

    private Imovel resolverImovel(Long imovelId) {

        if (imovelId == null) {
            return null;
        }

        return imovelRepository.findById(imovelId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Imóvel não encontrado."
                ));
    }

    private void limparUpload(
            String caminhoEnviado,
            Exception causa) {

        if (caminhoEnviado == null) {
            return;
        }

        try {
            supabaseStorageService.excluirArquivo(caminhoEnviado);
        } catch (Exception erroExclusao) {
            causa.addSuppressed(erroExclusao);
        }
    }

    private String extrairTexto(Path arquivo) throws IOException {
        if (pdfBoxExtrator.suporta(arquivo)) {
            String texto = pdfBoxExtrator.extrair(arquivo);

            if (texto != null && !texto.isBlank()) {
                return texto;
            }
        }

        if (tesseractExtrator.suporta(arquivo)) {
            String texto = tesseractExtrator.extrair(arquivo);

            if (texto != null && !texto.isBlank()) {
                return texto;
            }
        }

        throw new RegraDeNegocioExcecao(
                "Não foi possível extrair texto do arquivo."
        );
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraDeNegocioExcecao(
                    "Selecione um arquivo para enviar."
            );
        }

        String nome = obterNomeSeguro(arquivo.getOriginalFilename());
        String extensao = obterExtensao(nome).toLowerCase(Locale.ROOT);

        if (!extensao.matches(
                "\\.(pdf|png|jpg|jpeg|tif|tiff|bmp)")) {
            throw new RegraDeNegocioExcecao(
                    "Formato não permitido. Envie PDF ou imagem."
            );
        }
    }

    private String obterNomeSeguro(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O arquivo precisa ter um nome válido."
            );
        }

        String normalizado = nomeOriginal.replace("\\", "/");
        String nome = Path.of(normalizado).getFileName().toString();

        if (nome.isBlank() || nome.equals(".") || nome.equals("..")) {
            throw new RegraDeNegocioExcecao(
                    "Nome de arquivo inválido."
            );
        }

        return nome;
    }

    private String obterExtensao(String nome) {
        int indice = nome.lastIndexOf('.');

        if (indice < 0) {
            return "";
        }

        return nome.substring(indice).toLowerCase(Locale.ROOT);
    }

    public List<Documento> listarComFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            TipoDocumento tipo,
            Long imovelId,
            BigDecimal valorMinimo,
            BigDecimal valorMaximo) {

        validarFiltros(dataInicio, dataFim, valorMinimo, valorMaximo);

        return documentoRepository.findAll(
                DocumentoSpecifications.dataEmissaoEntre(
                        dataInicio, dataFim
                ).and(
                        DocumentoSpecifications.porTipo(tipo)
                ).and(
                        DocumentoSpecifications.porImovel(imovelId)
                ).and(
                        DocumentoSpecifications.valorEntre(
                                valorMinimo, valorMaximo
                        )
                )
        );
    }

    public Page<Documento> listarPaginadoComFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            TipoDocumento tipo,
            Long imovelId,
            BigDecimal valorMinimo,
            BigDecimal valorMaximo,
            Pageable pageable) {

        validarFiltros(dataInicio, dataFim, valorMinimo, valorMaximo);

        Specification<Documento> especificacao =
                DocumentoSpecifications.dataEmissaoEntre(dataInicio, dataFim)
                        .and(DocumentoSpecifications.porTipo(tipo))
                        .and(DocumentoSpecifications.porImovel(imovelId))
                        .and(DocumentoSpecifications.valorEntre(
                                valorMinimo, valorMaximo
                        ));

        return documentoRepository.findAll(especificacao, pageable);
    }

    private void validarFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal valorMinimo,
            BigDecimal valorMaximo) {

        if (dataInicio != null && dataFim != null
                && dataInicio.isAfter(dataFim)) {
            throw new RegraDeNegocioExcecao(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        if (valorMinimo != null && valorMaximo != null
                && valorMinimo.compareTo(valorMaximo) > 0) {
            throw new RegraDeNegocioExcecao(
                    "O valor mínimo não pode ser maior que o máximo."
            );
        }
    }
}
