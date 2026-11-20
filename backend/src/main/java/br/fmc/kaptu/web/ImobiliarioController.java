package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.imovel.*;
import br.fmc.kaptu.service.*;
import br.fmc.kaptu.web.dto.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/locatarios")
    public List<LocatarioDTO> listarLocatarios() {
        return locatarioService.listar()
                .stream()
                .map(this::toDTO)
                .toList();
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
                request.valorAluguel()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(contrato));
    }

    @GetMapping("/contratos")
    public List<ContratoLinhaDTO> listarContratos() {
        return contratoService.listar()
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
                contrato.getImovel().getNome(),
                contrato.getLocatario().getNome(),
                contrato.getStatusContrato(),
                contrato.getValorAluguel(),
                contrato.getDataValidade()
        );
    }
}