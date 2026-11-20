
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.documento.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentoRepository
        extends JpaRepository<Documento, Long>,
        JpaSpecificationExecutor<Documento> {
}
