package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ImovelRequest(
        @NotBlank String nome,
        @NotBlank String endereco,
        @NotBlank String bairro,
        @Positive int quantResidentes,
        @NotNull @DecimalMin("0.01") BigDecimal valorAluguelMensal
) {}