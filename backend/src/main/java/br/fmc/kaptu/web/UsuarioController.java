package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.usuario.Usuario;
import br.fmc.kaptu.service.UsuarioService;
import br.fmc.kaptu.web.dto.ErroRespostaDTO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public record UsuarioCriacaoRequest(
            String nome,
            String email,
            String senha,
            String perfil) {
    }

    public record UsuarioResposta(
            Long id,
            String nome,
            String email,
            boolean ativo,
            String perfil) {
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResposta>> listar(
            HttpServletRequest request) {

        Usuario solicitante = (Usuario) request.getAttribute("usuario");

        return ResponseEntity.ok(
                usuarioService.listar().stream()
                        .map(u -> new UsuarioResposta(
                                u.getId(),
                                u.getNome(),
                                u.getEmail(),
                                u.isAtivo(),
                                u.getClass().getSimpleName()
                        ))
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody UsuarioCriacaoRequest request,
            HttpServletRequest httpRequest) {

        Usuario solicitante =
                (Usuario) httpRequest.getAttribute("usuario");

        String perfil = request.perfil() == null
                ? "FUNCIONARIO"
                : request.perfil().trim().toUpperCase();

        if (!perfil.equals("GESTOR_ADMINISTRATIVO")
                && !perfil.equals("FUNCIONARIO")) {
            return ResponseEntity.badRequest().body(
                    new ErroRespostaDTO(
                            LocalDateTime.now(),
                            400,
                            "Bad Request",
                            "Perfil inválido.",
                            httpRequest.getRequestURI()
                    )
            );
        }

        try {
            Usuario criado = perfil.equals("GESTOR_ADMINISTRATIVO")
                    ? usuarioService.criarGestor(
                    solicitante,
                    request.nome(),
                    request.email(),
                    request.senha()
            )
                    : usuarioService.criarFuncionario(
                    solicitante,
                    request.nome(),
                    request.email(),
                    request.senha()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UsuarioResposta(
                            criado.getId(),
                            criado.getNome(),
                            criado.getEmail(),
                            criado.isAtivo(),
                            criado.getClass().getSimpleName()
                    ));

        } catch (RegraDeNegocioExcecao e) {
            return ResponseEntity.badRequest().body(
                    new ErroRespostaDTO(
                            LocalDateTime.now(),
                            400,
                            "Bad Request",
                            e.getMessage(),
                            httpRequest.getRequestURI()
                    )
            );
        }
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
