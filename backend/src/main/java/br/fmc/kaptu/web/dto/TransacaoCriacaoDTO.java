
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoCriacaoDTO(

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(
                value = "0.01",
                message = "O valor deve ser maior que zero."
        )
        BigDecimal valor,

        @NotNull(message = "A data é obrigatória.")
        LocalDate dataTransacao,

        @NotNull(message = "O tipo da transação é obrigatório.")
        TipoTransacao tipoTransacao

) {
}
