
package br.fmc.kaptu.domain.imovel;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImovelTest {

    private Imovel criarImovel() {
        return new Imovel(
                "Residencial Flores",
                "Rua das Flores, 100",
                "Centro",
                4,
                new BigDecimal("1500.00")
        );
    }

    @Test
    void deveIniciarComoDisponivel() {
        Imovel imovel = criarImovel();

        assertEquals(
                StatusOcupacao.DISPONIVEL,
                imovel.getStatusOcupacao()
        );
    }

    @Test
    void deveOcuparImovelDisponivel() {
        Imovel imovel = criarImovel();

        imovel.ocupar();

        assertEquals(
                StatusOcupacao.OCUPADO,
                imovel.getStatusOcupacao()
        );
    }

    @Test
    void naoDeveOcuparImovelJaOcupado() {
        Imovel imovel = criarImovel();
        imovel.ocupar();

        assertThrows(
                RegraDeNegocioExcecao.class,
                imovel::ocupar
        );
    }

    @Test
    void deveLiberarImovelOcupado() {
        Imovel imovel = criarImovel();
        imovel.ocupar();

        imovel.liberar();

        assertEquals(
                StatusOcupacao.DISPONIVEL,
                imovel.getStatusOcupacao()
        );
    }

    @Test
    void naoDeveLiberarImovelJaDisponivel() {
        Imovel imovel = criarImovel();

        assertThrows(
                RegraDeNegocioExcecao.class,
                imovel::liberar
        );
    }
}
