
package br.fmc.kaptu.domain.documento;

import br.fmc.kaptu.domain.imovel.Contrato;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
public class DocumentoContrato extends Documento {

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    private Integer prazoMeses;

    private LocalDate dataValidade;

    private BigDecimal valorAluguel;

    public DocumentoContrato() {
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.CONTRATO;
    }

    @Override
    public String getResumo() {
        return "Documento de contrato - "
                + (dataValidade != null
                ? dataValidade : "Data não identificada");
    }

    @Override
    public void aplicarCamposExtraidos(
            Map<String, String> campos
    ) {
        String prazo = campos.get("prazoMeses");
        String data = campos.get("dataValidade");
        String valor = campos.get("valorAluguel");

        try {
            if (prazo != null) {
                prazoMeses = Integer.valueOf(prazo);
            }

            if (data != null) {
                dataValidade = LocalDate.parse(data);
            }

            if (valor != null) {
                valorAluguel = new BigDecimal(
                        valor.replace(",", ".")
                );
                definirValorReferencia(valorAluguel);
            }

            if (prazoMeses == null
                    || dataValidade == null
                    || valorAluguel == null) {
                enviarParaRevisaoManual();
            }

        } catch (RuntimeException e) {
            enviarParaRevisaoManual();
        }
    }

    public void definirContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public Integer getPrazoMeses() {
        return prazoMeses;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public BigDecimal getValorAluguel() {
        return valorAluguel;
    }
}
