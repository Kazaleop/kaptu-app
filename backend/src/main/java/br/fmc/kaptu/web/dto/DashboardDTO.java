
package br.fmc.kaptu.web.dto;

import java.math.BigDecimal;

public record DashboardDTO(
        long totalImoveis,
        long imoveisOcupados,
        long contratosVigentes,
        long alertasNaoLidos,
        BigDecimal entradasMes,
        BigDecimal saidasMes,
        BigDecimal saldoMes
) {
}
