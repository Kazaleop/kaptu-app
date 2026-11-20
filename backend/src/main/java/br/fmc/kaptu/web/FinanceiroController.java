
package br.fmc.kaptu.web;

import br.fmc.kaptu.service.FinanceiroService;
import br.fmc.kaptu.web.dto.ConciliacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoCriacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoRespostaDTO;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(
            FinanceiroService financeiroService
    ) {
        this.financeiroService = financeiroService;
    }

    @PostMapping("/transacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoRespostaDTO lancarTransacao(
            @Valid @RequestBody TransacaoCriacaoDTO dto
    ) {
        return financeiroService.lancarTransacao(dto);
    }

    @GetMapping("/transacoes")
    public List<TransacaoRespostaDTO> listarTransacoes() {
        return financeiroService.listar();
    }

    @PostMapping("/conciliar")
    public TransacaoRespostaDTO conciliar(
            @Valid @RequestBody ConciliacaoDTO dto
    ) {
        return financeiroService
                .conciliarTransacaoComDocumento(dto);
    }
}
