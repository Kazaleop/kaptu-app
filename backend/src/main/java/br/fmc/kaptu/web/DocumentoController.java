package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.TipoDocumento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.ImovelRepository;
import br.fmc.kaptu.service.DocumentoService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;
    private final ImovelRepository imovelRepository;

    public DocumentoController(
            DocumentoService documentoService,
            DocumentoRepository documentoRepository,
            ImovelRepository imovelRepository) {

        this.documentoService = documentoService;
        this.documentoRepository = documentoRepository;
        this.imovelRepository = imovelRepository;
    }

    @PostMapping(
            value = {"/ocr", "/upload"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentoDTO> enviarDocumento(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "tipo", required = false)
            TipoDocumento tipo) {

        Documento documento =
                documentoService.processarUploadComOCR(arquivo, tipo);

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

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> buscarPorId(
            @PathVariable Long id) {

        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Documento não encontrado."
                ));

        return ResponseEntity.ok(DocumentoDTO.de(documento));
    }
}