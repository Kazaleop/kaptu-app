
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.repository.TransacaoRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    private RelatorioService relatorioService;

    @BeforeEach
    void configurar() {
        relatorioService = new RelatorioService(transacaoRepository);
    }

    @Test
    void deveGerarPdfComTransacoes() throws Exception {

        TransacaoFinanceira entrada = new TransacaoFinanceira(
                "Aluguel recebido",
                new BigDecimal("1500.00"),
                LocalDate.of(2026, 11, 10),
                TipoTransacao.ENTRADA
        );

        TransacaoFinanceira saida = new TransacaoFinanceira(
                "Manutencao do imovel",
                new BigDecimal("300.00"),
                LocalDate.of(2026, 11, 12),
                TipoTransacao.SAIDA
        );

        when(transacaoRepository.findAll())
                .thenReturn(List.of(entrada, saida));

        byte[] resultado = relatorioService.gerarRelatorioMpse(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        try (PDDocument pdf = Loader.loadPDF(resultado)) {
            assertTrue(pdf.getNumberOfPages() >= 1);
        }

        verify(transacaoRepository, times(1)).findAll();
    }

    @Test
    void deveGerarPdfMesmoSemTransacoes() throws Exception {

        when(transacaoRepository.findAll())
                .thenReturn(List.of());

        byte[] resultado = relatorioService.gerarRelatorioMpse(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertNotNull(resultado);

        try (PDDocument pdf = Loader.loadPDF(resultado)) {
            assertTrue(pdf.getNumberOfPages() >= 1);
        }
    }

    @Test
    void deveRejeitarPeriodoComDataInicialPosterior() {

        assertThrows(
                IllegalArgumentException.class,
                () -> relatorioService.gerarRelatorioMpse(
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 11, 1)
                )
        );

        verifyNoInteractions(transacaoRepository);
    }

    @Test
    void deveRejeitarDatasNulas() {

        assertThrows(
                IllegalArgumentException.class,
                () -> relatorioService.gerarRelatorioMpse(
                        null,
                        LocalDate.now()
                )
        );

        verifyNoInteractions(transacaoRepository);
    }
}
