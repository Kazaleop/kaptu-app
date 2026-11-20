
package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.NotNull;

public record ConciliacaoDTO(

        @NotNull(message = "O ID da transação é obrigatório.")
        Long transacaoId,

        @NotNull(message = "O ID do documento é obrigatório.")
        Long documentoId

) {
}
