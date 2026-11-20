package br.fmc.kaptu.web.dto;

import java.time.LocalDateTime;

public record ErroRespostaDTO(
        LocalDateTime dataHora,
        int status,
        String erro,
        String mensagem,
        String caminho
) {
}