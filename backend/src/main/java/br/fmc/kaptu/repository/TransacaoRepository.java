
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.domain.financeiro.TipoTransacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository
        extends JpaRepository<TransacaoFinanceira, Long> {

    @Query("""
            SELECT COALESCE(SUM(t.valor), 0)
            FROM TransacaoFinanceira t
            WHERE t.tipoTransacao = :tipo
            AND t.dataTransacao BETWEEN :inicio AND :fim
            """)
    BigDecimal somarNoPeriodo(
            @Param("tipo") TipoTransacao tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    List<TransacaoFinanceira>
    findByDataTransacaoBetweenOrderByDataTransacaoAsc(
            LocalDate inicio,
            LocalDate fim
    );
}
