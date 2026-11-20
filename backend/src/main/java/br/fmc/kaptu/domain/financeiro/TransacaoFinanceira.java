
package br.fmc.kaptu.domain.financeiro;

import br.fmc.kaptu.domain.base.EntidadeBase;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class TransacaoFinanceira extends EntidadeBase {

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataTransacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConciliacao statusConciliacao =
            StatusConciliacao.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    protected TransacaoFinanceira() {
    }

    public TransacaoFinanceira(
            String descricao,
            BigDecimal valor,
            LocalDate dataTransacao,
            TipoTransacao tipoTransacao
    ) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "A descrição não pode ser vazia."
            );
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioExcecao(
                    "O valor deve ser maior que zero."
            );
        }

        if (dataTransacao == null || tipoTransacao == null) {
            throw new RegraDeNegocioExcecao(
                    "A data e o tipo da transação são obrigatórios."
            );
        }

        this.descricao = descricao.trim();
        this.valor = valor;
        this.dataTransacao = dataTransacao;
        this.tipoTransacao = tipoTransacao;
    }

    public void conciliar(Documento documento) {
        if (documento == null) {
            throw new RegraDeNegocioExcecao(
                    "É necessário informar um documento para conciliar."
            );
        }

        if (estaConciliada()) {
            throw new RegraDeNegocioExcecao(
                    "Esta transação já está conciliada."
            );
        }

        this.documento = documento;
        this.statusConciliacao = StatusConciliacao.CONCILIADA;
    }

    public void desfazerConciliacao() {
        this.documento = null;
        this.statusConciliacao = StatusConciliacao.PENDENTE;
    }

    public boolean estaConciliada() {
        return statusConciliacao == StatusConciliacao.CONCILIADA;
    }

    public boolean isEntrada() {
        return tipoTransacao == TipoTransacao.ENTRADA;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getDataTransacao() {
        return dataTransacao;
    }

    public TipoTransacao getTipoTransacao() {
        return tipoTransacao;
    }

    public StatusConciliacao getStatusConciliacao() {
        return statusConciliacao;
    }

    public Documento getDocumento() {
        return documento;
    }
}
