package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.imovel.Locatario;
import br.fmc.kaptu.domain.util.Documentos;
import br.fmc.kaptu.repository.LocatarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
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

        String nomeNormalizado = nome == null ? null : nome.trim();
        String cpfNormalizado = Documentos.somenteDigitos(cpfCnpj);
        String telefoneNormalizado = Documentos.somenteDigitos(telefone);
        String emailNormalizado = email == null
                ? null
                : email.trim().toLowerCase();

        validar(nomeNormalizado, cpfNormalizado,
                telefoneNormalizado, emailNormalizado);

        if (repository.existsByCpfCnpj(cpfNormalizado)) {
            throw new RegraDeNegocioExcecao(
                    "CPF/CNPJ já cadastrado."
            );
        }

        try {
            return repository.save(new Locatario(
                    nomeNormalizado,
                    cpfNormalizado,
                    telefoneNormalizado,
                    emailNormalizado
            ));
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioExcecao(
                    "CPF/CNPJ já cadastrado."
            );
        }
    }

    public Locatario atualizar(
            Long id,
            String nome,
            String cpfCnpj,
            String telefone,
            String email) {

        Locatario locatario = buscarPorId(id);

        String nomeNormalizado = nome == null ? null : nome.trim();
        String cpfNormalizado = Documentos.somenteDigitos(cpfCnpj);
        String telefoneNormalizado = Documentos.somenteDigitos(telefone);
        String emailNormalizado = email == null
                ? null
                : email.trim().toLowerCase();

        validar(nomeNormalizado, cpfNormalizado,
                telefoneNormalizado, emailNormalizado);

        if (!cpfNormalizado.equals(locatario.getCpfCnpj())
                && repository.existsByCpfCnpj(cpfNormalizado)) {
            throw new RegraDeNegocioExcecao(
                    "CPF/CNPJ já cadastrado."
            );
        }

        // Atualiza via reflexão não é possível (campos private).
        // Recria a entidade preservando o id não é trivial; por ora,
        // apenas valida e retorna a existente quando não há mudança.
        // Para simplicidade, lança exceção de não implementado? 
        // Implementação completa exigiria setters; adicionamos setters.
        locatario.atualizarDados(
                nomeNormalizado,
                cpfNormalizado,
                telefoneNormalizado,
                emailNormalizado
        );

        return repository.save(locatario);
    }

    @Transactional(readOnly = true)
    public List<Locatario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Locatario> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listar();
        }

        String digitos = Documentos.somenteDigitos(termo);

        return repository.findAll().stream()
                .filter(l -> {
                    String nome = l.getNome() == null
                            ? ""
                            : l.getNome().toLowerCase();

                    if (nome.contains(termo.trim().toLowerCase())) {
                        return true;
                    }

                    if (digitos != null && !digitos.isBlank()) {
                        return l.getCpfCnpj() != null
                                && l.getCpfCnpj().contains(digitos);
                    }

                    return false;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Locatario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoExcecao(
                                "Locatário não encontrado."
                        ));
    }

    private void validar(
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

        boolean cpf = cpfCnpj.length() == 11;
        boolean cnpj = cpfCnpj.length() == 14;

        if (cpf && !Documentos.cpfValido(cpfCnpj)) {
            throw new RegraDeNegocioExcecao("CPF inválido.");
        }

        if (cnpj && !Documentos.cnpjValido(cpfCnpj)) {
            throw new RegraDeNegocioExcecao("CNPJ inválido.");
        }

        if (!cpf && !cnpj) {
            throw new RegraDeNegocioExcecao("CPF/CNPJ inválido.");
        }

        if (!Documentos.telefoneValido(telefone)) {
            throw new RegraDeNegocioExcecao(
                    "Telefone inválido. Informe DDD + número."
            );
        }
    }
}
