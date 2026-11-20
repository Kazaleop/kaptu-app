
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.financeiro.StatusConciliacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRespostaDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate dataTransacao,
        TipoTransacao tipoTransacao,
        StatusConciliacao statusConciliacao,
        Long documentoId
) {
    public static TransacaoRespostaDTO de(
            TransacaoFinanceira transacao
    ) {
        return new TransacaoRespostaDTO(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getDataTransacao(),
                transacao.getTipoTransacao(),
                transacao.getStatusConciliacao(),
                transacao.getDocumento() == null
                        ? null
                        : transacao.getDocumento().getId()
        );
    }
}
