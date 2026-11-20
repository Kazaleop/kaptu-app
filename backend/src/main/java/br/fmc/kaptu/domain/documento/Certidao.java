
package br.fmc.kaptu.domain.documento;

import br.fmc.kaptu.domain.base.Vencivel;
import br.fmc.kaptu.domain.alerta.TipoAlerta;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Map;

@Entity
public class Certidao extends Documento implements Vencivel {

    private String orgaoEmissor;

    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    private TipoCertidao tipoCertidao;

    public Certidao() {
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.CERTIDAO;
    }

    @Override
    public String getResumo() {
        return "Certidão " + tipoCertidao
                + " - Válida até " + dataValidade;
    }

    @Override
    public void aplicarCamposExtraidos(
            Map<String, String> campos
    ) {
        orgaoEmissor = campos.get("orgaoEmissor");

        try {
            String data = campos.get("dataValidade");
            String tipo = campos.get("tipoCertidao");

            if (data != null) {
                dataValidade = LocalDate.parse(data);
            }

            if (tipo != null) {
                tipoCertidao = TipoCertidao.valueOf(
                        tipo.toUpperCase()
                );
            }

            if (dataValidade == null || tipoCertidao == null) {
                enviarParaRevisaoManual();
            }

        } catch (RuntimeException e) {
            enviarParaRevisaoManual();
        }
    }

    @Override
    public LocalDate getDataVencimento() {
        return dataValidade;
    }

    @Override
    public TipoAlerta getTipoAlerta() {
        return TipoAlerta.CERTIDAO_A_VENCER;
    }

    @Override
    public String getDescricaoAlerta() {
        return "Certidão " + tipoCertidao
                + " vence em " + dataValidade;
    }

    public String getOrgaoEmissor() {
        return orgaoEmissor;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public TipoCertidao getTipoCertidao() {
        return tipoCertidao;
    }
}
