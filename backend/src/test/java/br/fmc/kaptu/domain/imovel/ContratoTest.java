
package br.fmc.kaptu.domain.imovel;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContratoTest {

    private Imovel criarImovel() {
        return new Imovel(
                "Residencial Flores",
                "Rua das Flores, 100",
                "Centro",
                4,
                new BigDecimal("1500.00")
        );
    }

    private Locatario criarLocatario() {
        return new Locatario(
                "João Silva",
                "12345678900",
                "92999999999",
                "joao@gmail.com"
        );
    }

    private Contrato criarContrato(Imovel imovel) {
        return new Contrato(
                imovel,
                criarLocatario(),
                LocalDate.of(2026, 11, 9),
                12,
                LocalDate.of(2027, 11, 9),
                new BigDecimal("1500.00")
        );
    }

    @Test
    void deveCriarContratoVigente() {
        Imovel imovel = criarImovel();

        Contrato contrato = criarContrato(imovel);

        assertEquals(
                StatusContrato.VIGENTE,
                contrato.getStatusContrato()
        );

        assertEquals(
                StatusOcupacao.OCUPADO,
                imovel.getStatusOcupacao()
        );
    }

    @Test
    void deveEncerrarContratoELiberarImovel() {
        Imovel imovel = criarImovel();
        Contrato contrato = criarContrato(imovel);

        contrato.encerrar();

        assertEquals(
                StatusContrato.ENCERRADO,
                contrato.getStatusContrato()
        );

        assertEquals(
                StatusOcupacao.DISPONIVEL,
                imovel.getStatusOcupacao()
        );
    }

    @Test
    void naoDeveEncerrarContratoDuasVezes() {
        Imovel imovel = criarImovel();
        Contrato contrato = criarContrato(imovel);

        contrato.encerrar();

        assertThrows(
                RegraDeNegocioExcecao.class,
                contrato::encerrar
        );
    }

    @Test
    void naoDeveCriarContratoComPrazoInvalido() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> new Contrato(
                        criarImovel(),
                        criarLocatario(),
                        LocalDate.of(2026, 11, 9),
                        0,
                        LocalDate.of(2027, 11, 9),
                        new BigDecimal("1500.00")
                )
        );
    }

    @Test
    void naoDeveCriarContratoComValorInvalido() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> new Contrato(
                        criarImovel(),
                        criarLocatario(),
                        LocalDate.of(2026, 11, 9),
                        12,
                        LocalDate.of(2027, 11, 9),
                        BigDecimal.ZERO
                )
        );
    }
}
