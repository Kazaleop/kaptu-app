package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.imovel.*;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ImovelService imovelService;
    private final LocatarioService locatarioService;

    public ContratoService(
            ContratoRepository contratoRepository,
            ImovelService imovelService,
            LocatarioService locatarioService) {

        this.contratoRepository = contratoRepository;
        this.imovelService = imovelService;
        this.locatarioService = locatarioService;
    }

    public Contrato criar(
            Long imovelId,
            Long locatarioId,
            LocalDate dataExpedicao,
            int prazoMeses,
            LocalDate dataValidade,
            BigDecimal valorAluguel,
            IndiceReajuste indiceReajuste) {

        Imovel imovel = imovelService.buscarPorId(imovelId);

        Locatario locatario =
                locatarioService.buscarPorId(locatarioId);

        if (contratoRepository.existsByImovel_IdAndStatusContrato(
                imovelId, StatusContrato.VIGENTE)
                || contratoRepository.existsByImovel_IdAndStatusContrato(
                imovelId, StatusContrato.VENCIDO)) {

            throw new RegraDeNegocioExcecao(
                    "Este imóvel já possui contrato em andamento."
            );
        }

        Contrato contrato = new Contrato(
                imovel,
                locatario,
                dataExpedicao,
                prazoMeses,
                dataValidade,
                valorAluguel,
                indiceReajuste
        );

        return contratoRepository.save(contrato);
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar(
            StatusContrato status,
            Long imovelId,
            Integer venceEmDias) {

        LocalDate hoje = LocalDate.now();

        return contratoRepository.findAll().stream()
                .filter(c -> status == null
                        || c.getStatusContrato() == status)
                .filter(c -> imovelId == null
                        || (c.getImovel() != null
                        && imovelId.equals(c.getImovel().getId())))
                .filter(c -> venceEmDias == null
                        || c.venceEm(venceEmDias, hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public Contrato buscarPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoExcecao(
                                "Contrato não encontrado."
                        ));
    }

    public void encerrar(Long id) {
        Contrato contrato = buscarPorId(id);
        contrato.encerrar();
    }

    public Contrato renovar(
            Long id,
            int prazoMeses,
            BigDecimal valorAluguel,
            IndiceReajuste indiceReajuste) {

        Contrato atual = buscarPorId(id);

        if (atual.getStatusContrato() != StatusContrato.VIGENTE
                && atual.getStatusContrato() != StatusContrato.VENCIDO) {
            throw new RegraDeNegocioExcecao(
                    "Somente contratos vigentes ou vencidos podem ser renovados."
            );
        }

        atual.encerrar();

        LocalDate novaExpedicao = atual.getDataValidade();
        LocalDate novaValidade = novaExpedicao.plusMonths(prazoMeses);

        BigDecimal novoValor = valorAluguel != null
                ? valorAluguel
                : atual.getValorAluguel();

        IndiceReajuste novoIndice = indiceReajuste != null
                ? indiceReajuste
                : atual.getIndiceReajuste();

        Contrato novo = new Contrato(
                atual.getImovel(),
                atual.getLocatario(),
                novaExpedicao,
                prazoMeses,
                novaValidade,
                novoValor,
                novoIndice
        );

        return contratoRepository.save(novo);
    }
}
