
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.imovel.Contrato;
import br.fmc.kaptu.domain.imovel.StatusContrato;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByImovel_IdAndStatusContrato(
            Long imovelId,
            StatusContrato statusContrato
    );

    long countByStatusContrato(StatusContrato statusContrato);
}
