package br.fmc.kaptu.web.dto;

import java.time.LocalDateTime;

public record LocatarioDTO(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        LocalDateTime dataCadastro
) {
}