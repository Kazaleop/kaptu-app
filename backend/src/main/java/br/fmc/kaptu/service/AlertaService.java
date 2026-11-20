package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.domain.alerta.TipoAlerta;
import br.fmc.kaptu.domain.base.Vencivel;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.imovel.Contrato;
import br.fmc.kaptu.domain.imovel.StatusContrato;
import br.fmc.kaptu.repository.AlertaRepository;
import br.fmc.kaptu.repository.ContratoRepository;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.TransacaoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaService {

    private static final Logger log =
            LoggerFactory.getLogger(AlertaService.class);

    private static final int DIAS_ANTECEDENCIA = 30;
    private static final int DIAS_REABERTURA = 15;
    private static final BigDecimal PERCENTUAL_ANOMALIA =
            new BigDecimal("30");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final AlertaRepository alertaRepository;
    private final ContratoRepository contratoRepository;
    private final DocumentoRepository documentoRepository;
    private final TransacaoRepository transacaoRepository;

    public AlertaService(
            AlertaRepository alertaRepository,
            ContratoRepository contratoRepository,
            DocumentoRepository documentoRepository,
            TransacaoRepository transacaoRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.contratoRepository = contratoRepository;
        this.documentoRepository = documentoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public void verificarVencimentos() {

        LocalDate hoje = LocalDate.now();

        marcarContratosVencidos(hoje);
        verificarContratos(hoje);
        verificarCertidoes(hoje);
        verificarAnomaliaFinanceira(hoje);
    }

    /**
     * Contratos com data de validade anterior a hoje passam para VENCIDO
     * e liberam o imóvel. Sem isso, o status nunca sairia de VIGENTE.
     */
    private void marcarContratosVencidos(LocalDate hoje) {

        List<Contrato> vencidos =
                contratoRepository
                        .findByStatusContratoAndDataValidadeBefore(
                                StatusContrato.VIGENTE,
                                hoje
                        );

        for (Contrato contrato : vencidos) {
            contrato.marcarVencido();
            log.info(
                    "Contrato {} marcado como vencido.",
                    contrato.getId()
            );
        }
    }

    private void verificarContratos(LocalDate hoje) {

        List<Contrato> contratos = contratoRepository.findAll();

        for (Contrato contrato : contratos) {

            if (contrato.getStatusContrato() != StatusContrato.VIGENTE) {
                continue;
            }

            verificarItemVencivel(contrato, hoje);
        }
    }

    private void verificarCertidoes(LocalDate hoje) {

        List<Documento> documentos = documentoRepository.findAll();

        for (Documento documento : documentos) {

            if (!(documento instanceof Vencivel vencivel)) {
                continue;
            }

            if (vencivel.getTipoAlerta() != TipoAlerta.CERTIDAO_A_VENCER) {
                continue;
            }

            verificarItemVencivel(vencivel, hoje);
        }
    }

    private void verificarItemVencivel(
            Vencivel item,
            LocalDate hoje
    ) {

        LocalDate vencimento = item.getDataVencimento();

        if (vencimento == null) {
            return;
        }

        long diasRestantes = item.diasParaVencer(hoje);

        if (diasRestantes < 0 || diasRestantes > DIAS_ANTECEDENCIA) {
            return;
        }

        registrarAlerta(
                item.getTipoAlerta(),
                item.getDescricaoAlerta(),
                item.getId()
        );
    }

    /**
     * Compara a média diária de saídas do mês corrente com a média diária
     * dos três meses anteriores. Variações acima de 30% geram um alerta
     * de anomalia financeira por mês.
     */
    private void verificarAnomaliaFinanceira(LocalDate hoje) {

        LocalDate inicioMes = hoje.withDayOfMonth(1);

        BigDecimal totalAtual = transacaoRepository.somarNoPeriodo(
                TipoTransacao.SAIDA,
                inicioMes,
                hoje
        );

        LocalDate inicioPeriodoAnterior = inicioMes.minusMonths(3);
        LocalDate fimPeriodoAnterior = inicioMes.minusDays(1);

        BigDecimal totalAnterior = transacaoRepository.somarNoPeriodo(
                TipoTransacao.SAIDA,
                inicioPeriodoAnterior,
                fimPeriodoAnterior
        );

        if (totalAtual == null || totalAnterior == null
                || totalAnterior.signum() <= 0) {
            return;
        }

        long diasAnteriores = ChronoUnit.DAYS.between(
                inicioPeriodoAnterior,
                inicioMes
        );

        if (diasAnteriores <= 0) {
            return;
        }

        BigDecimal mediaAtual = totalAtual.divide(
                BigDecimal.valueOf(hoje.getDayOfMonth()),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal mediaAnterior = totalAnterior.divide(
                BigDecimal.valueOf(diasAnteriores),
                2,
                RoundingMode.HALF_UP
        );

        if (mediaAnterior.signum() <= 0) {
            return;
        }

        BigDecimal variacao = mediaAtual
                .divide(mediaAnterior, 4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(CEM);

        if (variacao.compareTo(PERCENTUAL_ANOMALIA) < 0) {
            return;
        }

        long referenciaId = hoje.getYear() * 100L
                + hoje.getMonthValue();

        String mensagem = String.format(
                "Saídas do mês estão %.0f%% acima da média diária "
                        + "dos últimos três meses.",
                variacao.doubleValue()
        );

        registrarAlerta(
                TipoAlerta.ANOMALIA_FINANCEIRA,
                mensagem,
                referenciaId
        );
    }

    /**
     * Evita alertas duplicados. Se o alerta já foi lido, ele é reativado
     * após {@link #DIAS_REABERTURA} dias, enquanto a condição persistir.
     */
    private void registrarAlerta(
            TipoAlerta tipo,
            String mensagem,
            Long referenciaId
    ) {

        AlertaInteligente existente = alertaRepository
                .findFirstByTipoAlertaAndReferenciaIdOrderByDataGeracaoDesc(
                        tipo,
                        referenciaId
                )
                .orElse(null);

        if (existente != null) {

            if (!existente.isLido()) {
                return;
            }

            LocalDateTime limite = LocalDateTime.now()
                    .minusDays(DIAS_REABERTURA);

            if (existente.getDataGeracao().isAfter(limite)) {
                return;
            }

            existente.reativar(mensagem);
            return;
        }

        alertaRepository.save(
                new AlertaInteligente(tipo, mensagem, referenciaId)
        );
    }

    @Transactional(readOnly = true)
    public List<AlertaInteligente> listarTodos() {
        return alertaRepository.findAllByOrderByDataGeracaoDesc();
    }

    @Transactional(readOnly = true)
    public List<AlertaInteligente> listarNaoLidos() {
        return alertaRepository.findByLidoFalseOrderByDataGeracaoDesc();
    }

    @Transactional
    public void marcarComoLido(Long id) {

        AlertaInteligente alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Alerta não encontrado."
                ));

        alerta.marcarComoLido();
    }
}
