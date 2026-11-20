package br.fmc.kaptu.job;

import br.fmc.kaptu.domain.usuario.Administrador;
import br.fmc.kaptu.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DataLoader(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {

        String email = "admin@kaptu.org";

        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        String senhaHash = BCrypt.hashpw(
                "***REMOVED***",
                BCrypt.gensalt()
        );

        Administrador administrador = new Administrador(
                "Administrador Kaptu",
                email,
                senhaHash
        );

        usuarioRepository.save(administrador);

        System.out.println("Administrador inicial criado!");
    }
}