
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.documento.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoFiltroDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        TipoDocumento tipo,
        Long imovelId,
        BigDecimal valorMinimo,
        BigDecimal valorMaximo
) {
}
