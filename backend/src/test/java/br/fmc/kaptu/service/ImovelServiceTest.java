
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.repository.ImovelRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    @Mock
    private ImovelRepository repository;

    @InjectMocks
    private ImovelService service;

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
    void deveCadastrarImovelValido() {
        when(repository.save(any(Imovel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Imovel resultado = service.cadastrar(
                "Residencial Flores",
                "Rua das Flores, 100",
                "Centro",
                4,
                new BigDecimal("1500.00")
        );

        assertEquals("Residencial Flores", resultado.getNome());
        verify(repository).save(any(Imovel.class));
    }

    @Test
    void naoDeveCadastrarComNomeVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "", "Rua A", "Centro", 4,
                        new BigDecimal("1500")
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarComEnderecoVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "Imóvel", "", "Centro", 4,
                        new BigDecimal("1500")
                )
        );
    }

    @Test
    void naoDeveCadastrarComBairroVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "Imóvel", "Rua A", "", 4,
                        new BigDecimal("1500")
                )
        );
    }

    @Test
    void naoDeveCadastrarComQuantidadeDeResidentesInvalida() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "Imóvel", "Rua A", "Centro", 0,
                        new BigDecimal("1500")
                )
        );
    }

    @Test
    void naoDeveCadastrarComValorInvalido() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "Imóvel", "Rua A", "Centro", 4,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void deveListarImoveis() {
        when(repository.findAll())
                .thenReturn(List.of(criarImovel()));

        List<Imovel> resultado = service.listar();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void deveBuscarImovelExistente() {
        Imovel imovel = criarImovel();

        when(repository.findById(1L))
                .thenReturn(Optional.of(imovel));

        Imovel resultado = service.buscarPorId(1L);

        assertEquals(imovel, resultado);
    }

    @Test
    void deveLancarExcecaoQuandoImovelNaoExistir() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoExcecao.class,
                () -> service.buscarPorId(1L)
        );
    }
}
