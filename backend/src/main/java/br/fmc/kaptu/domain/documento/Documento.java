
package br.fmc.kaptu.domain.documento;

import br.fmc.kaptu.domain.base.EntidadeBase;
import br.fmc.kaptu.domain.imovel.Imovel;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Documento extends EntidadeBase {

    @Column(nullable = false)
    private String titulo;

    private LocalDate dataEmissao;

    private String caminhoArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusClassificacao statusClassificacao =
            StatusClassificacao.PENDENTE;

    @Column(length = 10000)
    private String textoExtraido;

    private BigDecimal valorReferencia;

    @ManyToOne
    @JoinColumn(name = "imovel_id")
    private Imovel imovel;

    public Documento() {
    }

    public abstract TipoDocumento getTipo();

    public abstract String getResumo();

    public abstract void aplicarCamposExtraidos(
            Map<String, String> campos
    );

    protected void definirValorReferencia(BigDecimal valor) {
        this.valorReferencia = valor;
    }

    public void marcarClassificado() {
        this.statusClassificacao = StatusClassificacao.CLASSIFICADO;
    }

    public void enviarParaRevisaoManual() {
        this.statusClassificacao =
                StatusClassificacao.REVISAO_MANUAL;
    }

    public void registrarErro() {
        this.statusClassificacao = StatusClassificacao.ERRO;
    }

    public void definirTextoExtraido(String texto) {
        this.textoExtraido = texto;
    }

    public void definirCaminhoArquivo(String caminho) {
        this.caminhoArquivo = caminho;
    }

    public void definirTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void definirDataEmissao(LocalDate data) {
        this.dataEmissao = data;
    }

    public void definirImovel(Imovel imovel) {
        this.imovel = imovel;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public StatusClassificacao getStatusClassificacao() {
        return statusClassificacao;
    }

    public String getTextoExtraido() {
        return textoExtraido;
    }

    public BigDecimal getValorReferencia() {
        return valorReferencia;
    }

    public Imovel getImovel() {
        return imovel;
    }
}
