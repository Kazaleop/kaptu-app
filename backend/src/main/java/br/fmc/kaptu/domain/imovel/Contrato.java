package br.fmc.kaptu.domain.imovel;

import br.fmc.kaptu.domain.base.EntidadeBase;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.fmc.kaptu.domain.base.Vencivel;
import br.fmc.kaptu.domain.alerta.TipoAlerta;

@Entity
public class Contrato extends EntidadeBase implements Vencivel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "imovel_id", nullable = false)
    private Imovel imovel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "locatario_id", nullable = false)
    private Locatario locatario;

    @Column(nullable = false)
    private LocalDate dataExpedicao;

    @Column(nullable = false)
    private int prazoMeses;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Column(nullable = false)
    private BigDecimal valorAluguel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusContrato statusContrato;

    protected Contrato() {
    }

    public Contrato(
            Imovel imovel,
            Locatario locatario,
            LocalDate dataExpedicao,
            int prazoMeses,
            LocalDate dataValidade,
            BigDecimal valorAluguel) {

        if (imovel == null || locatario == null) {
            throw new RegraDeNegocioExcecao(
                    "O imóvel e o locatário são obrigatórios."
            );
        }

        if (prazoMeses <= 0) {
            throw new RegraDeNegocioExcecao(
                    "O prazo do contrato deve ser maior que zero."
            );
        }

        if (valorAluguel == null || valorAluguel.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioExcecao(
                    "O valor do aluguel deve ser maior que zero."
            );
        }

        if (dataExpedicao == null || dataValidade == null) {
            throw new RegraDeNegocioExcecao(
                    "As datas do contrato são obrigatórias."
            );
        }

        this.imovel = imovel;
        this.locatario = locatario;
        this.dataExpedicao = dataExpedicao;
        this.prazoMeses = prazoMeses;
        this.dataValidade = dataValidade;
        this.valorAluguel = valorAluguel;
        this.statusContrato = StatusContrato.VIGENTE;

        this.imovel.ocupar();
    }

    public void encerrar() {
        if (statusContrato != StatusContrato.VIGENTE) {
            throw new RegraDeNegocioExcecao(
                    "Somente contratos vigentes podem ser encerrados."
            );
        }

        this.statusContrato = StatusContrato.ENCERRADO;
        this.imovel.liberar();
    }

    public Imovel getImovel() {
        return imovel;
    }

    public Locatario getLocatario() {
        return locatario;
    }

    public LocalDate getDataExpedicao() {
        return dataExpedicao;
    }

    public int getPrazoMeses() {
        return prazoMeses;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public BigDecimal getValorAluguel() {
        return valorAluguel;
    }

    public StatusContrato getStatusContrato() {
        return statusContrato;
    }

    @Override
    public LocalDate getDataVencimento() {
        return dataValidade;
    }

    @Override
    public TipoAlerta getTipoAlerta() {
        return TipoAlerta.CONTRATO_A_VENCER;
    }

    @Override
    public String getDescricaoAlerta() {
        return "Contrato de " + locatario.getNome()
                + " (" + imovel.getNome()
                + ") vence em " + dataValidade;
    }

    public boolean estaVencido(LocalDate hoje) {
        return hoje.isAfter(dataValidade);
    }

    public boolean venceEm(int dias, LocalDate hoje) {
        return !estaVencido(hoje)
                && !dataValidade.isAfter(hoje.plusDays(dias));
    }
}