
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.domain.alerta.TipoAlerta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository
        extends JpaRepository<AlertaInteligente, Long> {

    boolean existsByTipoAlertaAndReferenciaId(
            TipoAlerta tipoAlerta,
            Long referenciaId
    );

    Optional<AlertaInteligente>
    findFirstByTipoAlertaAndReferenciaIdOrderByDataGeracaoDesc(
            TipoAlerta tipoAlerta,
            Long referenciaId
    );

    long countByLidoFalse();

    List<AlertaInteligente> findAllByOrderByDataGeracaoDesc();

    List<AlertaInteligente> findByLidoFalseOrderByDataGeracaoDesc();
}
