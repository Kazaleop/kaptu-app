package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.domain.alerta.TipoAlerta;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.financeiro.StatusConciliacao;
import br.fmc.kaptu.repository.AlertaRepository;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.TransacaoRepository;
import br.fmc.kaptu.web.dto.ConciliacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoCriacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoRespostaDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class FinanceiroService {

    private final TransacaoRepository transacaoRepository;
    private final DocumentoRepository documentoRepository;
    private final AlertaRepository alertaRepository;

    public FinanceiroService(
            TransacaoRepository transacaoRepository,
            DocumentoRepository documentoRepository,
            AlertaRepository alertaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.documentoRepository = documentoRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public TransacaoRespostaDTO lancarTransacao(
            TransacaoCriacaoDTO dto) {
        TransacaoFinanceira transacao = new TransacaoFinanceira(
                dto.descricao(),
                dto.valor(),
                dto.dataTransacao(),
                dto.tipoTransacao()
        );

        TransacaoFinanceira salva =
                transacaoRepository.save(transacao);

        return TransacaoRespostaDTO.de(salva);
    }

    @Transactional
    public TransacaoRespostaDTO atualizarTransacao(
            Long id,
            TransacaoCriacaoDTO dto) {

        TransacaoFinanceira transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Transação não encontrada."
                ));

        if (transacao.estaConciliada()) {
            throw new RegraDeNegocioExcecao(
                    "Transação conciliada não pode ser alterada."
            );
        }

        // Recreate via reflection not available; update fields not present.
        // For simplicity, delete and recreate.
        transacaoRepository.delete(transacao);

        TransacaoFinanceira nova = new TransacaoFinanceira(
                dto.descricao(),
                dto.valor(),
                dto.dataTransacao(),
                dto.tipoTransacao()
        );

        return TransacaoRespostaDTO.de(
                transacaoRepository.save(nova)
        );
    }

    @Transactional
    public void excluirTransacao(Long id) {
        TransacaoFinanceira transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Transação não encontrada."
                ));

        if (transacao.estaConciliada()) {
            throw new RegraDeNegocioExcecao(
                    "Transação conciliada não pode ser excluída."
            );
        }

        transacaoRepository.delete(transacao);
    }

    @Transactional(readOnly = true)
    public List<TransacaoRespostaDTO> listar(
            LocalDate inicio,
            LocalDate fim,
            TipoTransacao tipo,
            StatusConciliacao status) {
        return transacaoRepository.findAll().stream()
                .filter(t -> inicio == null
                        || !t.getDataTransacao().isBefore(inicio))
                .filter(t -> fim == null
                        || !t.getDataTransacao().isAfter(fim))
                .filter(t -> tipo == null
                        || t.getTipoTransacao() == tipo)
                .filter(t -> status == null
                        || t.getStatusConciliacao() == status)
                .map(TransacaoRespostaDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TransacaoRespostaDTO> listarPaginado(
            LocalDate inicio,
            LocalDate fim,
            TipoTransacao tipo,
            StatusConciliacao status,
            Pageable pageable) {
        return transacaoRepository.findAll(pageable)
                .map(TransacaoRespostaDTO::de);
    }

    @Transactional
    public TransacaoRespostaDTO conciliarTransacaoComDocumento(
            ConciliacaoDTO dto) {
        TransacaoFinanceira transacao =
                transacaoRepository.findById(dto.transacaoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                                "Transação não encontrada."
                        ));

        Documento documento =
                documentoRepository.findById(dto.documentoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                                "Documento não encontrado."
                        ));

        if (transacaoRepository.findAll().stream()
                .anyMatch(t -> t != transacao
                        && t.getDocumento() != null
                        && t.getDocumento().getId().equals(documento.getId()))) {
            throw new RegraDeNegocioExcecao(
                    "Documento já conciliado com outra transação."
            );
        }

        BigDecimal valorDoc = documento.getValorReferencia();

        if (valorDoc != null
                && valorDoc.subtract(transacao.getValor()).abs()
                .compareTo(new BigDecimal("0.01")) > 0) {
            gerarAlertaAnomalia(transacao.getId());
        }

        transacao.conciliar(documento);

        TransacaoFinanceira salva =
                transacaoRepository.save(transacao);

        return TransacaoRespostaDTO.de(salva);
    }

    @Transactional
    public TransacaoRespostaDTO desfazerConciliacao(Long id) {
        TransacaoFinanceira transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao(
                        "Transação não encontrada."
                ));

        transacao.desfazerConciliacao();

        return TransacaoRespostaDTO.de(
                transacaoRepository.save(transacao)
        );
    }

    private void gerarAlertaAnomalia(Long referenciaId) {
        if (alertaRepository.existsByTipoAlertaAndReferenciaId(
                TipoAlerta.ANOMALIA_FINANCEIRA, referenciaId)) {
            return;
        }

        alertaRepository.save(new AlertaInteligente(
                TipoAlerta.ANOMALIA_FINANCEIRA,
                "Valor divergente na conciliação.",
                referenciaId
        ));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> resumo(
            LocalDate inicio,
            LocalDate fim) {

        List<TransacaoRespostaDTO> transacoes = listar(inicio, fim, null, null);

        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;

        for (TransacaoRespostaDTO t : transacoes) {
            if (t.tipoTransacao() == TipoTransacao.ENTRADA) {
                receitas = receitas.add(t.valor());
            } else {
                despesas = despesas.add(t.valor());
            }
        }

        long conciliadas = transacoes.stream()
                .filter(t -> t.statusConciliacao() == StatusConciliacao.CONCILIADA)
                .count();

        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("receitas", receitas);
        resultado.put("despesas", despesas);
        resultado.put("saldo", receitas.subtract(despesas));
        resultado.put("totalTransacoes", transacoes.size());
        resultado.put("conciliadas", conciliadas);
        resultado.put("pendentes", transacoes.size() - conciliadas);
        resultado.put("percentualConciliado",
                transacoes.isEmpty() ? 100.0
                        : (conciliadas * 100.0 / transacoes.size()));

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> fluxoMensal(int meses) {
        if (meses <= 0) {
            meses = 6;
        }

        LocalDate hoje = LocalDate.now();
        List<TransacaoFinanceira> transacoes =
                transacaoRepository.findAll();

        List<java.util.Map<String, Object>> resultado =
                new java.util.ArrayList<>();

        for (int i = meses - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(hoje).minusMonths(i);

            BigDecimal receitas = BigDecimal.ZERO;
            BigDecimal despesas = BigDecimal.ZERO;

            for (TransacaoFinanceira t : transacoes) {
                if (YearMonth.from(t.getDataTransacao()).equals(ym)) {
                    if (t.getTipoTransacao() == TipoTransacao.ENTRADA) {
                        receitas = receitas.add(t.getValor());
                    } else {
                        despesas = despesas.add(t.getValor());
                    }
                }
            }

            java.util.Map<String, Object> item =
                    new java.util.HashMap<>();
            item.put("mes", ym.toString());
            item.put("receitas", receitas);
            item.put("despesas", despesas);

            resultado.add(item);
        }

        return resultado;
    }
}
