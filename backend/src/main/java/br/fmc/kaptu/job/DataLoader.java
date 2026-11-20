package br.fmc.kaptu.job;

import br.fmc.kaptu.domain.usuario.Administrador;
import br.fmc.kaptu.repository.UsuarioRepository;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DataLoader.class);

    private static final int TAMANHO_MINIMO_SENHA = 12;

    private final UsuarioRepository usuarioRepository;
    private final String email;
    private final String senha;

    public DataLoader(
            UsuarioRepository usuarioRepository,
            @Value("${kaptu.admin.email:}") String email,
            @Value("${kaptu.admin.senha:}") String senha) {

        this.usuarioRepository = usuarioRepository;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {

            log.info(
                    "Administrador inicial não criado: "
                            + "KAPTU_ADMIN_EMAIL e KAPTU_ADMIN_SENHA "
                            + "não estão definidos."
            );
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn(
                    "Administrador inicial não criado: a senha deve "
                            + "ter pelo menos {} caracteres.",
                    TAMANHO_MINIMO_SENHA
            );
            return;
        }

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            log.info(
                    "Administrador inicial já existe para o e-mail "
                            + "configurado."
            );
            return;
        }

        String senhaHash = BCrypt.hashpw(
                senha,
                BCrypt.gensalt()
        );

        Administrador administrador = new Administrador(
                "Administrador Kaptu",
                emailNormalizado,
                senhaHash
        );

        usuarioRepository.save(administrador);

        log.info(
                "Administrador inicial criado para o e-mail configurado."
        );
    }
}
