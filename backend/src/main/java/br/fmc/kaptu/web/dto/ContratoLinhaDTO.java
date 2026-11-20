package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.imovel.StatusContrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoLinhaDTO(
        Long id,
        Long imovelId,
        String imovel,
        String locatario,
        StatusContrato status,
        BigDecimal valor,
        LocalDate dataValidade
) {
}