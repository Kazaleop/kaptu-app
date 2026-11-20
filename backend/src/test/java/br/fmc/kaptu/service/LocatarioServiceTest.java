
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.imovel.Locatario;
import br.fmc.kaptu.repository.LocatarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocatarioServiceTest {

    @Mock
    private LocatarioRepository repository;

    @InjectMocks
    private LocatarioService service;

    private Locatario criarLocatario() {
        return new Locatario(
                "João Silva",
                "12345678900",
                "92999999999",
                "joao@gmail.com"
        );
    }

    @Test
    void deveCadastrarLocatarioValido() {
        when(repository.existsByCpfCnpj("12345678900"))
                .thenReturn(false);

        when(repository.save(any(Locatario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Locatario resultado = service.cadastrar(
                "João Silva",
                "12345678900",
                "92999999999",
                "joao@gmail.com"
        );

        assertEquals("João Silva", resultado.getNome());
        verify(repository).save(any(Locatario.class));
    }

    @Test
    void naoDeveCadastrarComNomeVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "", "12345678900",
                        "92999999999", "joao@gmail.com"
                )
        );
    }

    @Test
    void naoDeveCadastrarComCpfVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "João", "", "92999999999",
                        "joao@gmail.com"
                )
        );
    }

    @Test
    void naoDeveCadastrarComTelefoneVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "João", "12345678900", "",
                        "joao@gmail.com"
                )
        );
    }

    @Test
    void naoDeveCadastrarComEmailVazio() {
        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "João", "12345678900",
                        "92999999999", ""
                )
        );
    }

    @Test
    void naoDeveCadastrarCpfDuplicado() {
        when(repository.existsByCpfCnpj("12345678900"))
                .thenReturn(true);

        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.cadastrar(
                        "João Silva",
                        "12345678900",
                        "92999999999",
                        "joao@gmail.com"
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveListarLocatarios() {
        when(repository.findAll())
                .thenReturn(List.of(criarLocatario()));

        List<Locatario> resultado = service.listar();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarLocatarioExistente() {
        Locatario locatario = criarLocatario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(locatario));

        Locatario resultado = service.buscarPorId(1L);

        assertEquals(locatario, resultado);
    }

    @Test
    void deveLancarExcecaoQuandoLocatarioNaoExistir() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoExcecao.class,
                () -> service.buscarPorId(1L)
        );
    }
}
