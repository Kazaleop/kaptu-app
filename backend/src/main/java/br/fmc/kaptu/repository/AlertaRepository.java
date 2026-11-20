
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.domain.alerta.TipoAlerta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository
        extends JpaRepository<AlertaInteligente, Long> {

    boolean existsByTipoAlertaAndReferenciaId(
            TipoAlerta tipoAlerta,
            Long referenciaId
    );

    long countByLidoFalse();

    List<AlertaInteligente> findAllByOrderByDataGeracaoDesc();

    List<AlertaInteligente> findByLidoFalseOrderByDataGeracaoDesc();
}
