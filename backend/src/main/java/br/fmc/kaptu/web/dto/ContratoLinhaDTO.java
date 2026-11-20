package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.imovel.IndiceReajuste;
import br.fmc.kaptu.domain.imovel.StatusContrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoLinhaDTO(
        Long id,
        Long imovelId,
        Long locatarioId,
        String imovel,
        String locatario,
        StatusContrato status,
        BigDecimal valor,
        LocalDate dataExpedicao,
        int prazoMeses,
        LocalDate dataValidade,
        IndiceReajuste indiceReajuste
) {
}
