package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.imovel.Locatario;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.repository.LocatarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LocatarioService {

    private final LocatarioRepository repository;

    public LocatarioService(LocatarioRepository repository) {
        this.repository = repository;
    }

    public Locatario cadastrar(
            String nome,
            String cpfCnpj,
            String telefone,
            String email) {

        if (nome == null || nome.isBlank()
                || cpfCnpj == null || cpfCnpj.isBlank()
                || telefone == null || telefone.isBlank()
                || email == null || email.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "Todos os campos são obrigatórios."
            );
        }

        if (repository.existsByCpfCnpj(cpfCnpj)) {
            throw new RegraDeNegocioExcecao(
                    "CPF/CNPJ já cadastrado."
            );
        }

        return repository.save(
                new Locatario(nome, cpfCnpj, telefone, email)
        );
    }

    @Transactional(readOnly = true)
    public List<Locatario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Locatario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoExcecao(
                                "Locatário não encontrado."
                        ));
    }
}