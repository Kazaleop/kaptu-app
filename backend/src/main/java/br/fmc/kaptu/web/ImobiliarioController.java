package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.imovel.*;
import br.fmc.kaptu.service.*;
import br.fmc.kaptu.web.dto.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ImobiliarioController {

    private final ImovelService imovelService;
    private final LocatarioService locatarioService;
    private final ContratoService contratoService;

    public ImobiliarioController(
            ImovelService imovelService,
            LocatarioService locatarioService,
            ContratoService contratoService) {

        this.imovelService = imovelService;
        this.locatarioService = locatarioService;
        this.contratoService = contratoService;
    }

    @PostMapping("/imoveis")
    public ResponseEntity<ImovelDTO> cadastrarImovel(
            @Valid @RequestBody ImovelRequest request) {

        Imovel imovel = imovelService.cadastrar(
                request.nome(),
                request.endereco(),
                request.bairro(),
                request.quantResidentes(),
                request.valorAluguelMensal()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(imovel));
    }

    @GetMapping("/imoveis")
    public List<ImovelDTO> listarImoveis() {
        return imovelService.listar()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/imoveis/{id}")
    public ImovelDTO buscarImovel(@PathVariable Long id) {
        return toDTO(imovelService.buscarPorId(id));
    }

    @PutMapping("/imoveis/{id}")
    public ImovelDTO atualizarImovel(
            @PathVariable Long id,
            @Valid @RequestBody ImovelRequest request) {
        return toDTO(imovelService.atualizar(
                id,
                request.nome(),
                request.endereco(),
                request.bairro(),
                request.quantResidentes(),
                request.valorAluguelMensal()
        ));
    }

    @DeleteMapping("/imoveis/{id}")
    public ResponseEntity<Void> excluirImovel(@PathVariable Long id) {
        imovelService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/locatarios")
    public ResponseEntity<LocatarioDTO> cadastrarLocatario(
            @Valid @RequestBody LocatarioRequest request) {

        Locatario locatario = locatarioService.cadastrar(
                request.nome(),
                request.cpfCnpj(),
                request.telefone(),
                request.email()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(locatario));
    }

    @GetMapping("/locatarios/{id}")
    public LocatarioDTO buscarLocatario(@PathVariable Long id) {
        return toDTO(locatarioService.buscarPorId(id));
    }

    @GetMapping("/locatarios")
    public List<LocatarioDTO> listarLocatarios() {
        return locatarioService.listar()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping(value = "/locatarios", params = "busca")
    public List<LocatarioDTO> buscarLocatarios(
            @RequestParam String busca) {
        return locatarioService.buscar(busca)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PutMapping("/locatarios/{id}")
    public LocatarioDTO atualizarLocatario(
            @PathVariable Long id,
            @Valid @RequestBody LocatarioRequest request) {

        Locatario locatario = locatarioService.atualizar(
                id,
                request.nome(),
                request.cpfCnpj(),
                request.telefone(),
                request.email()
        );

        return toDTO(locatario);
    }

    @PostMapping("/contratos")
    public ResponseEntity<ContratoLinhaDTO> criarContrato(
            @Valid @RequestBody ContratoRequest request) {

        LocalDate dataExpedicao = request.dataExpedicao() != null
                ? request.dataExpedicao()
                : LocalDate.now();

        LocalDate dataValidade =
                dataExpedicao.plusMonths(request.prazoMeses());

        Contrato contrato = contratoService.criar(
                request.imovelId(),
                request.locatarioId(),
                dataExpedicao,
                request.prazoMeses(),
                dataValidade,
                request.valorAluguel(),
                request.indiceReajuste()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(contrato));
    }

    @GetMapping("/contratos/{id}")
    public ContratoLinhaDTO buscarContrato(@PathVariable Long id) {
        return toDTO(contratoService.buscarPorId(id));
    }

    @GetMapping("/contratos")
    public List<ContratoLinhaDTO> listarContratos(
            @RequestParam(required = false) StatusContrato status,
            @RequestParam(required = false) Long imovelId,
            @RequestParam(required = false) Integer venceEmDias) {
        return contratoService.listar(status, imovelId, venceEmDias)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PostMapping("/contratos/{id}/encerrar")
    public ResponseEntity<Void> encerrarContrato(
            @PathVariable Long id) {

        contratoService.encerrar(id);

        return ResponseEntity.noContent().build();
    }

    public record RenovacaoRequest(
            int prazoMeses,
            BigDecimal valorAluguel,
            IndiceReajuste indiceReajuste) {
    }

    @PostMapping("/contratos/{id}/renovar")
    public ResponseEntity<ContratoLinhaDTO> renovarContrato(
            @PathVariable Long id,
            @RequestBody RenovacaoRequest request) {

        Contrato contrato = contratoService.renovar(
                id,
                request.prazoMeses(),
                request.valorAluguel(),
                request.indiceReajuste()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(contrato));
    }

    private ImovelDTO toDTO(Imovel imovel) {
        return new ImovelDTO(
                imovel.getId(),
                imovel.getNome(),
                imovel.getEndereco(),
                imovel.getBairro(),
                imovel.getQuantResidentes(),
                imovel.getValorAluguelMensal(),
                imovel.getStatusOcupacao()
        );
    }

    private LocatarioDTO toDTO(Locatario locatario) {
        return new LocatarioDTO(
                locatario.getId(),
                locatario.getNome(),
                locatario.getCpfCnpj(),
                locatario.getTelefone(),
                locatario.getEmail(),
                locatario.getCriadoEm()
        );
    }

    private ContratoLinhaDTO toDTO(Contrato contrato) {
        return new ContratoLinhaDTO(
                contrato.getId(),
                contrato.getImovel().getId(),
                contrato.getLocatario().getId(),
                contrato.getImovel().getNome(),
                contrato.getLocatario().getNome(),
                contrato.getStatusContrato(),
                contrato.getValorAluguel(),
                contrato.getDataExpedicao(),
                contrato.getPrazoMeses(),
                contrato.getDataValidade(),
                contrato.getIndiceReajuste()
        );
    }
}