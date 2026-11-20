package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.usuario.*;
import br.fmc.kaptu.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public GestorAdministrativo criarGestor(Usuario solicitante, String nome, String email, String senha) {
        verificarAdministrador(solicitante);
        validarDados(nome, email, senha);
        verificarEmailDisponivel(email);

        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

        GestorAdministrativo gestor =
                new GestorAdministrativo(nome, email, senhaHash);

        return usuarioRepository.save(gestor);
    }

    public Funcionario criarFuncionario(
            Usuario solicitante,
            String nome,
            String email,
            String senha) {

        verificarAdministrador(solicitante);
        validarDados(nome, email, senha);
        verificarEmailDisponivel(email);

        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

        Funcionario funcionario =
                new Funcionario(nome, email, senhaHash);

        return usuarioRepository.save(funcionario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public void ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.ativar();
    }

    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.desativar();
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() ->
                        new RecursoNaoEncontradoExcecao(
                                "Usuário não encontrado."
                        ));
    }

    private void verificarAdministrador(Usuario solicitante) {
        if (solicitante == null ||
                !solicitante.podeExecutar(Permissao.GERIR_USUARIOS)) {

            throw new RegraDeNegocioExcecao(
                    "Apenas um Administrador pode gerenciar usuários."
            );
        }
    }

    private void validarDados(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O nome não pode ser vazio."
            );
        }

        if (email == null || email.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "O e-mail não pode ser vazio."
            );
        }

        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "A senha não pode ser vazia."
            );
        }
    }

    private void verificarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraDeNegocioExcecao(
                    "Este e-mail já está cadastrado."
            );
        }
    }
}