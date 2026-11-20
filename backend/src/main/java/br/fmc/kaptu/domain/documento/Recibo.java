
package br.fmc.kaptu.domain.documento;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Map;

@Entity
public class Recibo extends Documento {

    private BigDecimal valor;

    private String pagador;

    private String favorecido;

    private String numTransacao;

    public Recibo() {
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.RECIBO;
    }

    @Override
    public String getResumo() {
        return "Recibo - " + favorecido
                + " - Valor: " + valor;
    }

    @Override
    public void aplicarCamposExtraidos(
            Map<String, String> campos
    ) {
        pagador = campos.get("pagador");
        favorecido = campos.get("favorecido");
        numTransacao = campos.get("numTransacao");

        String valorTexto = campos.get("valor");

        try {
            if (valorTexto != null) {
                valor = new BigDecimal(
                        valorTexto.replace(",", ".")
                );

                definirValorReferencia(valor);
            } else {
                enviarParaRevisaoManual();
            }
        } catch (NumberFormatException e) {
            enviarParaRevisaoManual();
        }
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getPagador() {
        return pagador;
    }

    public String getFavorecido() {
        return favorecido;
    }

    public String getNumTransacao() {
        return numTransacao;
    }
}
