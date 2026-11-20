package br.fmc.kaptu.domain.imovel;

import br.fmc.kaptu.domain.base.EntidadeBase;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Imovel extends EntidadeBase {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private int quantResidentes;

    @Column(nullable = false)
    private BigDecimal valorAluguelMensal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOcupacao statusOcupacao = StatusOcupacao.DISPONIVEL;

    protected Imovel() {
    }

    public Imovel(
            String nome,
            String endereco,
            String bairro,
            int quantResidentes,
            BigDecimal valorAluguelMensal) {

        this.nome = nome;
        this.endereco = endereco;
        this.bairro = bairro;
        this.quantResidentes = quantResidentes;
        this.valorAluguelMensal = valorAluguelMensal;
    }

    public void ocupar() {
        if (statusOcupacao == StatusOcupacao.OCUPADO) {
            throw new RegraDeNegocioExcecao(
                    "Este imóvel já está ocupado."
            );
        }

        this.statusOcupacao = StatusOcupacao.OCUPADO;
    }

    public void liberar() {
        if (statusOcupacao == StatusOcupacao.DISPONIVEL) {
            throw new RegraDeNegocioExcecao(
                    "Este imóvel já está disponível."
            );
        }

        this.statusOcupacao = StatusOcupacao.DISPONIVEL;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public int getQuantResidentes() {
        return quantResidentes;
    }

    public BigDecimal getValorAluguelMensal() {
        return valorAluguelMensal;
    }

    public StatusOcupacao getStatusOcupacao() {
        return statusOcupacao;
    }
}