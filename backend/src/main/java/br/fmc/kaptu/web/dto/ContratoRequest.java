package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoRequest(
        @NotNull Long imovelId,
        @NotNull Long locatarioId,
        @NotNull @Positive Integer prazoMeses,
        @NotNull @DecimalMin("0.01") BigDecimal valorAluguel,
        LocalDate dataExpedicao
) {}