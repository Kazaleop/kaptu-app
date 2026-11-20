
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.domain.alerta.TipoAlerta;
import br.fmc.kaptu.domain.base.Vencivel;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.imovel.Contrato;
import br.fmc.kaptu.domain.imovel.StatusContrato;
import br.fmc.kaptu.repository.AlertaRepository;
import br.fmc.kaptu.repository.ContratoRepository;
import br.fmc.kaptu.repository.DocumentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlertaService {

    private static final int DIAS_ANTECEDENCIA = 30;

    private final AlertaRepository alertaRepository;
    private final ContratoRepository contratoRepository;
    private final DocumentoRepository documentoRepository;

    public AlertaService(
            AlertaRepository alertaRepository,
            ContratoRepository contratoRepository,
            DocumentoRepository documentoRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.contratoRepository = contratoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional
    public void verificarVencimentos() {

        LocalDate hoje = LocalDate.now();

        verificarContratos(hoje);
        verificarCertidoes(hoje);
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

        boolean alertaExiste =
                alertaRepository.existsByTipoAlertaAndReferenciaId(
                        item.getTipoAlerta(),
                        item.getId()
                );

        if (alertaExiste) {
            return;
        }

        AlertaInteligente alerta = new AlertaInteligente(
                item.getTipoAlerta(),
                item.getDescricaoAlerta(),
                item.getId()
        );

        alertaRepository.save(alerta);
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
