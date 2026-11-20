package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ImovelRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String endereco,
        @NotBlank @Size(max = 255) String bairro,
        @Positive @Max(10000) int quantResidentes,
        @NotNull @DecimalMin("0.01") BigDecimal valorAluguelMensal
) {}
