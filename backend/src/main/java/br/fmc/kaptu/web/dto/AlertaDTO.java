
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.alerta.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaDTO(
        Long id,
        TipoAlerta tipo,
        String mensagem,
        Long referenciaId,
        LocalDateTime dataGeracao,
        boolean lido
) {
}
