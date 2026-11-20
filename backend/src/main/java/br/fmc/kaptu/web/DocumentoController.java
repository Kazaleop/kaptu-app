package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.TipoDocumento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.ImovelRepository;
import br.fmc.kaptu.repository.TransacaoRepository;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.service.DocumentoService;
import br.fmc.kaptu.service.SupabaseStorageService;
import br.fmc.kaptu.web.dto.DocumentoDTO;
import br.fmc.kaptu.web.dto.LocalizacaoDocumentoDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoController {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DocumentoController.class);

    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;
    private final ImovelRepository imovelRepository;
    private final TransacaoRepository transacaoRepository;
    private final SupabaseStorageService supabaseStorageService;

    public DocumentoController(
            DocumentoService documentoService,
            DocumentoRepository documentoRepository,
            ImovelRepository imovelRepository,
            TransacaoRepository transacaoRepository,
            SupabaseStorageService supabaseStorageService) {

        this.documentoService = documentoService;
        this.documentoRepository = documentoRepository;
        this.imovelRepository = imovelRepository;
        this.transacaoRepository = transacaoRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping(
            value = {"/ocr", "/upload"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentoDTO> enviarDocumento(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "tipo", required = false)
            TipoDocumento tipo,
            @RequestParam(value = "imovelId", required = false)
            Long imovelId,
            @RequestParam(value = "dataEmissao", required = false)
            LocalDate dataEmissao) {

        Documento documento =
                documentoService.processarUploadComOCR(
                        arquivo,
                        tipo,
                        imovelId,
                        dataEmissao
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DocumentoDTO.de(documento));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentoDTO>> listarDocumentos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) TipoDocumento tipo,
            @RequestParam(required = false) Long imovelId,
            @RequestParam(required = false) BigDecimal valorMinimo,
            @RequestParam(required = false) BigDecimal valorMaximo,
            @RequestParam(required = false) String busca,
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<DocumentoDTO> documentos =
                documentoService.listarPaginadoComFiltros(
                        dataInicio,
                        dataFim,
                        tipo,
                        imovelId,
                        valorMinimo,
                        valorMaximo,
                        busca,
                        pageable
                ).map(DocumentoDTO::de);

        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/localizacoes")
    public ResponseEntity<List<LocalizacaoDocumentoDTO>>
    listarLocalizacoes() {

        List<LocalizacaoDocumentoDTO> localizacoes =
                imovelRepository.findAll()
                        .stream()
                        .map(imovel -> new LocalizacaoDocumentoDTO(
                                imovel.getId(),
                                imovel.getNome(),
                                imovel.getBairro()
                        ))
                        .toList();

        return ResponseEntity.ok(localizacoes);
    }

    @Deprecated
    @GetMapping("/filtros")
    public ResponseEntity<List<DocumentoDTO>> listarComFiltros(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) TipoDocumento tipo,
            @RequestParam(required = false) Long imovelId,
            @RequestParam(required = false) BigDecimal valorMinimo,
            @RequestParam(required = false) BigDecimal valorMaximo) {

        List<DocumentoDTO> documentos =
                documentoService.listarComFiltros(
                                dataInicio,
                                dataFim,
                                tipo,
                                imovelId,
                                valorMinimo,
                                valorMaximo
                        )
                        .stream()
                        .map(DocumentoDTO::de)
                        .toList();

        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{id}/arquivo")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Long id) throws IOException {

        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Documento não encontrado."
                ));

        if (documento.getCaminhoArquivo() == null
                || documento.getCaminhoArquivo().isBlank()) {
            throw new RecursoNaoEncontradoExcecao(
                    "Documento não possui arquivo."
            );
        }

        SupabaseStorageService.ArquivoBaixado arquivo =
                supabaseStorageService.baixarArquivo(
                        documento.getCaminhoArquivo()
                );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        arquivo.tipoConteudo()
                ))
                .header("Content-Disposition",
                        "inline; filename=\""
                                + documento.getTitulo() + "\"")
                .body(arquivo.conteudo());
    }

    public record DocumentoAtualizacaoRequest(
            TipoDocumento tipo,
            String titulo,
            LocalDate dataEmissao,
            BigDecimal valorReferencia,
            Long imovelId) {
    }

    @PutMapping("/{id}")
    public DocumentoDTO atualizarDocumento(
            @PathVariable Long id,
            @RequestBody DocumentoAtualizacaoRequest request) {

        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Documento não encontrado."
                ));

        if (request.titulo() != null
                && !request.titulo().isBlank()) {
            documento.definirTitulo(request.titulo());
        }

        if (request.dataEmissao() != null) {
            documento.definirDataEmissao(request.dataEmissao());
        }

        if (request.valorReferencia() != null) {
            documento.definirValorReferencia(request.valorReferencia());
        }

        if (request.imovelId() != null) {
            Imovel imovel = imovelRepository.findById(
                    request.imovelId()
            ).orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                    "Imóvel não encontrado."
            ));

            documento.definirImovel(imovel);
        }

        documento.marcarClassificado();

        return DocumentoDTO.de(documentoRepository.save(documento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable Long id) {

        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Documento não encontrado."
                ));

        boolean conciliado = transacaoRepository
                .existsByDocumento_Id(id);

        if (conciliado) {
            throw new RegraDeNegocioExcecao(
                    "Documento vinculado a uma transação."
            );
        }

        if (documento.getCaminhoArquivo() != null
                && !documento.getCaminhoArquivo().isBlank()) {
            try {
                supabaseStorageService.excluirArquivo(
                        documento.getCaminhoArquivo()
                );
            } catch (IOException e) {
                log.warn("Falha ao excluir arquivo: {}", e.getMessage());
            }
        }

        documentoRepository.delete(documento);

        return ResponseEntity.noContent().build();
    }
}