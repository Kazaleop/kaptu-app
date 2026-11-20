
package br.fmc.kaptu.domain.documento;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Map;

@Entity
public class NotaFiscal extends Documento {

    private String cnpjEmissor;
    private String numeroNota;

    private BigDecimal valorTotal;

    private String chaveAcesso;

    public NotaFiscal() {
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.NOTA_FISCAL;
    }

    @Override
    public String getResumo() {
        return "Nota fiscal " + numeroNota
                + " - Valor: " + valorTotal;
    }

    @Override
    public void aplicarCamposExtraidos(
            Map<String, String> campos
    ) {
        cnpjEmissor = campos.get("cnpjEmissor");
        numeroNota = campos.get("numeroNota");
        chaveAcesso = campos.get("chaveAcesso");

        String valor = campos.get("valorTotal");

        if (valor != null && !valor.isBlank()) {
            try {
                valorTotal = new BigDecimal(
                        valor.replace(",", ".")
                );
                definirValorReferencia(valorTotal);
            } catch (NumberFormatException e) {
                enviarParaRevisaoManual();
            }
        } else {
            enviarParaRevisaoManual();
        }
    }

    public String getCnpjEmissor() {
        return cnpjEmissor;
    }

    public String getNumeroNota() {
        return numeroNota;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }
}
