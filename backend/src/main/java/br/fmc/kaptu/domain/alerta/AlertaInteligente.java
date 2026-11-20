
package br.fmc.kaptu.domain.alerta;

import br.fmc.kaptu.domain.base.EntidadeBase;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AlertaInteligente extends EntidadeBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private Long referenciaId;

    @Column(nullable = false)
    private LocalDateTime dataGeracao;

    @Column(nullable = false)
    private boolean lido = false;

    protected AlertaInteligente() {
    }

    public AlertaInteligente(
            TipoAlerta tipoAlerta,
            String mensagem,
            Long referenciaId
    ) {
        if (tipoAlerta == null || mensagem == null
                || mensagem.isBlank() || referenciaId == null) {
            throw new RegraDeNegocioExcecao(
                    "Os dados do alerta são obrigatórios."
            );
        }

        this.tipoAlerta = tipoAlerta;
        this.mensagem = mensagem;
        this.referenciaId = referenciaId;
        this.dataGeracao = LocalDateTime.now();
    }

    public void marcarComoLido() {
        this.lido = true;
    }

    /**
     * Atualiza um alerta já existente e o marca como não lido.
     * Usado para reenviar um lembrete quando a condição persiste.
     */
    public void reativar(String novaMensagem) {
        if (novaMensagem != null && !novaMensagem.isBlank()) {
            this.mensagem = novaMensagem.trim();
        }

        this.dataGeracao = LocalDateTime.now();
        this.lido = false;
    }

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public boolean isLido() {
        return lido;
    }
}
