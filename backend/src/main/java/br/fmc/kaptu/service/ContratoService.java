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
            BigDecimal valorAluguel) {

        Imovel imovel = imovelService.buscarPorId(imovelId);

        Locatario locatario =
                locatarioService.buscarPorId(locatarioId);

        if (contratoRepository.existsByImovel_IdAndStatusContrato(
                imovelId, StatusContrato.VIGENTE)) {

            throw new RegraDeNegocioExcecao(
                    "Este imóvel já possui contrato vigente."
            );
        }

        Contrato contrato = new Contrato(
                imovel,
                locatario,
                dataExpedicao,
                prazoMeses,
                dataValidade,
                valorAluguel
        );

        return contratoRepository.save(contrato);
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return contratoRepository.findAll();
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
}