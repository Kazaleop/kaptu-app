package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.repository.ImovelRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ImovelService {

    private final ImovelRepository imovelRepository;

    public ImovelService(ImovelRepository imovelRepository) {
        this.imovelRepository = imovelRepository;
    }

    public Imovel cadastrar(
            String nome,
            String endereco,
            String bairro,
            int quantResidentes,
            BigDecimal valorAluguelMensal) {

        validarDados(
                nome,
                endereco,
                bairro,
                quantResidentes,
                valorAluguelMensal
        );

        Imovel imovel = new Imovel(
                nome,
                endereco,
                bairro,
                quantResidentes,
                valorAluguelMensal
        );

        return imovelRepository.save(imovel);
    }

    @Transactional(readOnly = true)
    public List<Imovel> listar() {
        return imovelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Imovel buscarPorId(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoExcecao(
                                "Imóvel não encontrado."
                        ));
    }

    private void validarDados(
            String nome,
            String endereco,
            String bairro,
            int quantResidentes,
            BigDecimal valorAluguelMensal) {

        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O nome do imóvel é obrigatório."
            );
        }

        if (endereco == null || endereco.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O endereço é obrigatório."
            );
        }

        if (bairro == null || bairro.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O bairro é obrigatório."
            );
        }

        if (quantResidentes <= 0) {
            throw new RegraDeNegocioExcecao(
                    "A quantidade de residentes deve ser maior que zero."
            );
        }

        if (valorAluguelMensal == null ||
                valorAluguelMensal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RegraDeNegocioExcecao(
                    "O valor do aluguel deve ser maior que zero."
            );
        }
    }
}