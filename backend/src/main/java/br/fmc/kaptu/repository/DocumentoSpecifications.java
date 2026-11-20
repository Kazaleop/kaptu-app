
package br.fmc.kaptu.repository;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.TipoDocumento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DocumentoSpecifications {

    private DocumentoSpecifications() {
    }

    public static Specification<Documento> dataEmissaoEntre(
            LocalDate inicio,
            LocalDate fim) {

        return (root, query, criteriaBuilder) -> {
            if (inicio == null && fim == null) {
                return criteriaBuilder.conjunction();
            }

            if (inicio != null && fim != null) {
                return criteriaBuilder.between(
                        root.get("dataEmissao"), inicio, fim
                );
            }

            if (inicio != null) {
                return criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dataEmissao"), inicio
                );
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("dataEmissao"), fim
            );
        };
    }

    public static Specification<Documento> porTipo(
            TipoDocumento tipo) {

        return (root, query, criteriaBuilder) -> {
            if (tipo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.type(), classeDoTipo(tipo)
            );
        };
    }

    public static Specification<Documento> porImovel(
            Long imovelId) {

        return (root, query, criteriaBuilder) -> {
            if (imovelId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("imovel").get("id"), imovelId
            );
        };
    }

    public static Specification<Documento> valorEntre(
            BigDecimal minimo,
            BigDecimal maximo) {

        return (root, query, criteriaBuilder) -> {
            if (minimo == null && maximo == null) {
                return criteriaBuilder.conjunction();
            }

            if (minimo != null && maximo != null) {
                return criteriaBuilder.between(
                        root.get("valorReferencia"),
                        minimo,
                        maximo
                );
            }

            if (minimo != null) {
                return criteriaBuilder.greaterThanOrEqualTo(
                        root.get("valorReferencia"), minimo
                );
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("valorReferencia"), maximo
            );
        };
    }

    private static Class<? extends Documento> classeDoTipo(
            TipoDocumento tipo) {

        return switch (tipo) {
            case NOTA_FISCAL ->
                    br.fmc.kaptu.domain.documento.NotaFiscal.class;
            case CONTRATO ->
                    br.fmc.kaptu.domain.documento.DocumentoContrato.class;
            case RECIBO ->
                    br.fmc.kaptu.domain.documento.Recibo.class;
            case CERTIDAO ->
                    br.fmc.kaptu.domain.documento.Certidao.class;
        };
    }
}
