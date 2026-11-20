package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.usuario.Usuario;
import br.fmc.kaptu.repository.UsuarioRepository;
import br.fmc.kaptu.service.TokenService;
import br.fmc.kaptu.web.dto.ErroRespostaDTO;

import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            TokenService tokenService) {

        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    public record LoginRequest(String email, String senha) {
    }

    public record UsuarioResposta(
            Long id,
            String nome,
            String email,
            String perfil,
            List<String> permissoes) {
    }

    public record LoginResponse(
            String token,
            long expiraEm,
            UsuarioResposta usuario) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String email = request.email() == null
                ? null
                : request.email().trim().toLowerCase();

        String senha = request.senha();

        Usuario usuario = email == null
                ? null
                : usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !usuario.isAtivo()
                || senha == null
                || !BCrypt.checkpw(senha, usuario.getSenhaHash())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroRespostaDTO(
                            LocalDateTime.now(),
                            401,
                            "Unauthorized",
                            "E-mail ou senha inválidos.",
                            httpRequest.getRequestURI()
                    ));
        }

        String token = tokenService.gerarToken(usuario);

        LoginResponse resposta = new LoginResponse(
                token,
                tokenService.getExpiracaoSegundos(),
                new UsuarioResposta(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getClass().getSimpleName(),
                        usuario.getPermissoes().stream()
                                .map(Enum::name)
                                .toList()
                )
        );

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {

        Usuario usuario = (Usuario) request.getAttribute("usuario");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroRespostaDTO(
                            LocalDateTime.now(),
                            401,
                            "Unauthorized",
                            "Sessão expirada ou inválida. Faça login novamente.",
                            request.getRequestURI()
                    ));
        }

        return ResponseEntity.ok(new UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getClass().getSimpleName(),
                usuario.getPermissoes().stream()
                        .map(Enum::name)
                        .toList()
        ));
    }
}
