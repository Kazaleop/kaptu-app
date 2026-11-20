package br.fmc.kaptu.config;

import br.fmc.kaptu.domain.excecao.AcessoNegadoExcecao;
import br.fmc.kaptu.domain.usuario.Usuario;
import br.fmc.kaptu.domain.usuario.Permissao;
import br.fmc.kaptu.repository.UsuarioRepository;
import br.fmc.kaptu.service.TokenService;
import br.fmc.kaptu.web.dto.ErroRespostaDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final boolean habilitado;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AuthFilter(
            @Value("${kaptu.auth.enabled:true}") boolean habilitado,
            TokenService tokenService,
            UsuarioRepository usuarioRepository) {

        this.habilitado = habilitado;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!habilitado) {
            filterChain.doFilter(request, response);
            return;
        }

        String caminho = request.getRequestURI();

        if (!caminho.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/api/v1/auth/login".equals(caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null
                || !cabecalho.startsWith("Bearer ")) {

            responder401(response, request,
                    "Sessão expirada ou inválida. Faça login novamente.");
            return;
        }

        String token = cabecalho.substring(7).trim();

        Long usuarioId = tokenService.validarEExtrairUsuarioId(token);

        if (usuarioId == null) {
            responder401(response, request,
                    "Sessão expirada ou inválida. Faça login novamente.");
            return;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (usuario == null || !usuario.isAtivo()) {
            responder401(response, request,
                    "Sessão expirada ou inválida. Faça login novamente.");
            return;
        }

        Permissao permissaoNecessaria = permissaoPara(caminho, request.getMethod());

        if (permissaoNecessaria != null
                && !usuario.podeExecutar(permissaoNecessaria)) {
            responder403(response, request);
            return;
        }

        request.setAttribute("usuario", usuario);
        filterChain.doFilter(request, response);
    }

    private void responder401(
            HttpServletResponse response,
            HttpServletRequest request,
            String mensagem) throws IOException {

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");

        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                401,
                "Unauthorized",
                mensagem,
                request.getRequestURI()
        );

        response.getWriter().write(toJson(erro));
    }

    private void responder403(
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");

        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                403,
                "Forbidden",
                "Você não tem permissão para esta ação.",
                request.getRequestURI()
        );

        response.getWriter().write(toJson(erro));
    }

    private String toJson(ErroRespostaDTO erro) {
        return "{"
                + "\"dataHora\":\"" + erro.dataHora() + "\","
                + "\"status\":" + erro.status() + ","
                + "\"erro\":\"" + erro.erro() + "\","
                + "\"mensagem\":\"" + escapar(erro.mensagem()) + "\","
                + "\"caminho\":\"" + escapar(erro.caminho()) + "\""
                + "}";
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private Permissao permissaoPara(String caminho, String metodo) {
        boolean leitura = "GET".equalsIgnoreCase(metodo);

        if (caminho.startsWith("/api/v1/usuarios")
                || caminho.startsWith("/api/v1/alertas")) {
            if (leitura) {
                // alertas listagem = CONSULTAR
                if (caminho.startsWith("/api/v1/alertas")) {
                    return Permissao.CONSULTAR;
                }
                return Permissao.GERIR_USUARIOS;
            }
            return Permissao.GERIR_USUARIOS;
        }

        if (caminho.contains("/dashboard")) {
            return leitura ? Permissao.VER_DASHBOARD : Permissao.CONSULTAR;
        }

        if (caminho.contains("/relatorio")) {
            return Permissao.VER_RELATORIOS;
        }

        if (leitura) {
            return Permissao.CONSULTAR;
        }

        if (caminho.contains("/documentos")) {
            return Permissao.ENVIAR_DOCUMENTOS;
        }

        if (caminho.contains("/financeiro/transacoes")) {
            return Permissao.LANCAR_TRANSACOES;
        }

        if (caminho.contains("/financeiro/conciliar")
                || caminho.contains("desfazer-conciliacao")) {
            return Permissao.CONCILIAR;
        }

        return Permissao.CADASTRAR;
    }
}
