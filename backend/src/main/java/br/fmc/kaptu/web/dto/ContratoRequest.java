package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.imovel.IndiceReajuste;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoRequest(
        @NotNull Long imovelId,
        @NotNull Long locatarioId,
        @NotNull @Positive @Max(1200) Integer prazoMeses,
        @NotNull @DecimalMin("0.01") BigDecimal valorAluguel,
        LocalDate dataExpedicao,
        IndiceReajuste indiceReajuste
) {}
