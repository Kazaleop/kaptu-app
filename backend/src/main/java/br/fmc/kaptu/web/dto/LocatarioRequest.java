package br.fmc.kaptu.web.dto;

import jakarta.validation.constraints.*;

public record LocatarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 20) String cpfCnpj,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 254) String email
) {}
