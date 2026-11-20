
package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.documento.Certidao;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.DocumentoContrato;
import br.fmc.kaptu.domain.documento.StatusClassificacao;
import br.fmc.kaptu.domain.documento.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentoDTO(
        Long id,
        String titulo,
        TipoDocumento tipo,
        StatusClassificacao statusClassificacao,
        LocalDate dataEmissao,
        BigDecimal valorReferencia,
        String resumo,
        String textoExtraido,
        Long imovelId,
        String imovelNome,
        LocalDate dataValidade,
        LocalDateTime criadoEm,
        boolean possuiArquivo
) {

    public static DocumentoDTO de(Documento documento) {
        LocalDate dataValidade = null;

        if (documento instanceof DocumentoContrato dc) {
            dataValidade = dc.getDataValidade();
        } else if (documento instanceof Certidao c) {
            dataValidade = c.getDataValidade();
        }

        return new DocumentoDTO(
                documento.getId(),
                documento.getTitulo(),
                documento.getTipo(),
                documento.getStatusClassificacao(),
                documento.getDataEmissao(),
                documento.getValorReferencia(),
                documento.getResumo(),
                documento.getTextoExtraido(),
                documento.getImovel() != null
                        ? documento.getImovel().getId()
                        : null,
                documento.getImovel() != null
                        ? documento.getImovel().getNome()
                        : null,
                dataValidade,
                documento.getCriadoEm(),
                documento.getCaminhoArquivo() != null
                        && !documento.getCaminhoArquivo().isBlank()
        );
    }
}
