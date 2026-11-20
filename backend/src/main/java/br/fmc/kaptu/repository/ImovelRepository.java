package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.domain.imovel.StatusOcupacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    long countByStatusOcupacao(
            StatusOcupacao statusOcupacao
    );
}