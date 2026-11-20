package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.*;

public record LocatarioRequest(
        @NotBlank String nome,
        @NotBlank String cpfCnpj,
        @NotBlank String telefone,
        @NotBlank @Email String email
) {}