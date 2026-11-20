
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.StatusClassificacao;
import br.fmc.kaptu.domain.documento.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoDTO(
        Long id,
        String titulo,
        TipoDocumento tipo,
        StatusClassificacao statusClassificacao,
        LocalDate dataEmissao,
        BigDecimal valorReferencia,
        String resumo,
        String textoExtraido
) {

    public static DocumentoDTO de(Documento documento) {
        return new DocumentoDTO(
                documento.getId(),
                documento.getTitulo(),
                documento.getTipo(),
                documento.getStatusClassificacao(),
                documento.getDataEmissao(),
                documento.getValorReferencia(),
                documento.getResumo(),
                documento.getTextoExtraido()
        );
    }
}
