package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.financeiro.StatusConciliacao;
import br.fmc.kaptu.service.FinanceiroService;
import br.fmc.kaptu.web.dto.ConciliacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoCriacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoRespostaDTO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @PostMapping("/transacoes")
    public ResponseEntity<TransacaoRespostaDTO> lancar(
            @RequestBody TransacaoCriacaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financeiroService.lancarTransacao(dto));
    }

    @GetMapping("/transacoes")
    public List<TransacaoRespostaDTO> listar(
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) StatusConciliacao status) {
        return financeiroService.listar(inicio, fim, tipo, status);
    }

    @PutMapping("/transacoes/{id}")
    public TransacaoRespostaDTO atualizar(
            @PathVariable Long id,
            @RequestBody TransacaoCriacaoDTO dto) {
        return financeiroService.atualizarTransacao(id, dto);
    }

    @DeleteMapping("/transacoes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        financeiroService.excluirTransacao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conciliar")
    public TransacaoRespostaDTO conciliar(
            @RequestBody ConciliacaoDTO dto) {
        return financeiroService.conciliarTransacaoComDocumento(dto);
    }

    @PostMapping("/transacoes/{id}/desfazer-conciliacao")
    public TransacaoRespostaDTO desfazer(
            @PathVariable Long id) {
        return financeiroService.desfazerConciliacao(id);
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo(
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim) {
        return financeiroService.resumo(inicio, fim);
    }

    @GetMapping("/fluxo-mensal")
    public List<Map<String, Object>> fluxoMensal(
            @RequestParam(defaultValue = "6") int meses) {
        return financeiroService.fluxoMensal(meses);
    }
}
