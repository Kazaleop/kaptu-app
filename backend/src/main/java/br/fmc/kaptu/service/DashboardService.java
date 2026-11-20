
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.imovel.StatusContrato;
import br.fmc.kaptu.domain.documento.StatusClassificacao;
import br.fmc.kaptu.domain.imovel.StatusOcupacao;
import br.fmc.kaptu.domain.financeiro.TipoTransacao;

import br.fmc.kaptu.repository.DocumentoRepository;

import br.fmc.kaptu.repository.ImovelRepository;
import br.fmc.kaptu.repository.ContratoRepository;
import br.fmc.kaptu.repository.AlertaRepository;
import br.fmc.kaptu.repository.TransacaoRepository;

import br.fmc.kaptu.web.dto.DashboardDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final ImovelRepository imovelRepository;
    private final ContratoRepository contratoRepository;
    private final AlertaRepository alertaRepository;
    private final TransacaoRepository transacaoRepository;
    private final DocumentoRepository documentoRepository;

    public DashboardService(
            ImovelRepository imovelRepository,
            ContratoRepository contratoRepository,
            AlertaRepository alertaRepository,
            TransacaoRepository transacaoRepository,
            DocumentoRepository documentoRepository
    ) {
        this.imovelRepository = imovelRepository;
        this.contratoRepository = contratoRepository;
        this.alertaRepository = alertaRepository;
        this.transacaoRepository = transacaoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDTO gerarResumo() {

        LocalDate hoje = LocalDate.now();

        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(
                hoje.lengthOfMonth()
        );

        long totalImoveis = imovelRepository.count();

        long imoveisOcupados =
                imovelRepository.countByStatusOcupacao(
                        StatusOcupacao.OCUPADO
                );

        long contratosVigentes =
                contratoRepository.countByStatusContrato(
                        StatusContrato.VIGENTE
                );

        long alertasNaoLidos =
                alertaRepository.countByLidoFalse();

        BigDecimal entradas = transacaoRepository.somarNoPeriodo(
                TipoTransacao.ENTRADA,
                inicioMes,
                fimMes
        );

        BigDecimal saidas = transacaoRepository.somarNoPeriodo(
                TipoTransacao.SAIDA,
                inicioMes,
                fimMes
        );

        BigDecimal saldo = entradas.subtract(saidas);

        long contratosAVencer30Dias = contratoRepository.findAll().stream()
                .filter(c -> c.getStatusContrato() == StatusContrato.VIGENTE)
                .filter(c -> !c.getDataValidade().isAfter(hoje.plusDays(30)))
                .count();

        long documentosTotal = documentoRepository.count();

        long documentosClassificados = documentoRepository.findAll().stream()
                .filter(d -> d.getStatusClassificacao()
                        == StatusClassificacao.CLASSIFICADO)
                .count();

        long documentosAguardandoRevisao = documentosTotal
                - documentosClassificados;

        return new DashboardDTO(
                totalImoveis,
                imoveisOcupados,
                contratosVigentes,
                alertasNaoLidos,
                entradas,
                saidas,
                saldo,
                contratosAVencer30Dias,
                documentosTotal,
                documentosClassificados,
                documentosAguardandoRevisao
        );
    }
}
