
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.imovel.*;
import br.fmc.kaptu.repository.ContratoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ImovelService imovelService;

    @Mock
    private LocatarioService locatarioService;

    @InjectMocks
    private ContratoService service;

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
    void deveCriarContratoValido() {
        Imovel imovel = criarImovel();
        Locatario locatario = criarLocatario();

        when(imovelService.buscarPorId(1L))
                .thenReturn(imovel);

        when(locatarioService.buscarPorId(1L))
                .thenReturn(locatario);

        when(contratoRepository
                .existsByImovel_IdAndStatusContrato(
                        1L, StatusContrato.VIGENTE))
                .thenReturn(false);

        when(contratoRepository.save(any(Contrato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Contrato resultado = service.criar(
                1L,
                1L,
                LocalDate.of(2026, 11, 9),
                12,
                LocalDate.of(2027, 11, 9),
                new BigDecimal("1500.00")
        );

        assertEquals(
                StatusContrato.VIGENTE,
                resultado.getStatusContrato()
        );

        assertEquals(
                StatusOcupacao.OCUPADO,
                imovel.getStatusOcupacao()
        );

        verify(contratoRepository).save(any(Contrato.class));
    }

    @Test
    void naoDeveCriarContratoParaImovelComContratoVigente() {
        when(imovelService.buscarPorId(1L))
                .thenReturn(criarImovel());

        when(locatarioService.buscarPorId(1L))
                .thenReturn(criarLocatario());

        when(contratoRepository
                .existsByImovel_IdAndStatusContrato(
                        1L, StatusContrato.VIGENTE))
                .thenReturn(true);

        assertThrows(
                RegraDeNegocioExcecao.class,
                () -> service.criar(
                        1L,
                        1L,
                        LocalDate.of(2026, 11, 9),
                        12,
                        LocalDate.of(2027, 11, 9),
                        new BigDecimal("1500.00")
                )
        );

        verify(contratoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarContratoQuandoImovelNaoExistir() {
        when(imovelService.buscarPorId(1L))
                .thenThrow(new RecursoNaoEncontradoExcecao(
                        "Imóvel não encontrado."
                ));

        assertThrows(
                RecursoNaoEncontradoExcecao.class,
                () -> service.criar(
                        1L,
                        1L,
                        LocalDate.of(2026, 11, 9),
                        12,
                        LocalDate.of(2027, 11, 9),
                        new BigDecimal("1500.00")
                )
        );

        verify(contratoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarContratoQuandoLocatarioNaoExistir() {
        when(imovelService.buscarPorId(1L))
                .thenReturn(criarImovel());

        when(locatarioService.buscarPorId(1L))
                .thenThrow(new RecursoNaoEncontradoExcecao(
                        "Locatário não encontrado."
                ));

        assertThrows(
                RecursoNaoEncontradoExcecao.class,
                () -> service.criar(
                        1L,
                        1L,
                        LocalDate.of(2026, 11, 9),
                        12,
                        LocalDate.of(2027, 11, 9),
                        new BigDecimal("1500.00")
                )
        );

        verify(contratoRepository, never()).save(any());
    }

    @Test
    void deveListarContratos() {
        Imovel imovel = criarImovel();

        when(contratoRepository.findAll())
                .thenReturn(List.of(criarContrato(imovel)));

        List<Contrato> resultado = service.listar();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarContratoExistente() {
        Contrato contrato = criarContrato(criarImovel());

        when(contratoRepository.findById(1L))
                .thenReturn(Optional.of(contrato));

        Contrato resultado = service.buscarPorId(1L);

        assertEquals(contrato, resultado);
    }

    @Test
    void deveLancarExcecaoQuandoContratoNaoExistir() {
        when(contratoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoExcecao.class,
                () -> service.buscarPorId(1L)
        );
    }

    @Test
    void deveEncerrarContrato() {
        Imovel imovel = criarImovel();
        Contrato contrato = criarContrato(imovel);

        when(contratoRepository.findById(1L))
                .thenReturn(Optional.of(contrato));

        service.encerrar(1L);

        assertEquals(
                StatusContrato.ENCERRADO,
                contrato.getStatusContrato()
        );

        assertEquals(
                StatusOcupacao.DISPONIVEL,
                imovel.getStatusOcupacao()
        );
    }
}
