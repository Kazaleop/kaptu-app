
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.repository.DocumentoRepository;
import br.fmc.kaptu.repository.TransacaoRepository;
import br.fmc.kaptu.web.dto.ConciliacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoCriacaoDTO;
import br.fmc.kaptu.web.dto.TransacaoRespostaDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinanceiroService {

    private final TransacaoRepository transacaoRepository;
    private final DocumentoRepository documentoRepository;

    public FinanceiroService(
            TransacaoRepository transacaoRepository,
            DocumentoRepository documentoRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional
    public TransacaoRespostaDTO lancarTransacao(
            TransacaoCriacaoDTO dto
    ) {
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

    @Transactional(readOnly = true)
    public List<TransacaoRespostaDTO> listar() {
        return transacaoRepository.findAll()
                .stream()
                .map(TransacaoRespostaDTO::de)
                .toList();
    }

    @Transactional
    public TransacaoRespostaDTO conciliarTransacaoComDocumento(
            ConciliacaoDTO dto
    ) {
        TransacaoFinanceira transacao =
                transacaoRepository.findById(dto.transacaoId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoExcecao(
                                        "Transação não encontrada."
                                )
                        );

        Documento documento =
                documentoRepository.findById(dto.documentoId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoExcecao(
                                        "Documento não encontrado."
                                )
                        );

        transacao.conciliar(documento);

        TransacaoFinanceira salva =
                transacaoRepository.save(transacao);

        return TransacaoRespostaDTO.de(salva);
    }
}
