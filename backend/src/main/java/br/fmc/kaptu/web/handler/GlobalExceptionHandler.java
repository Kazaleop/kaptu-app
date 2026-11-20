package br.fmc.kaptu.web.handler;

import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.web.dto.ErroRespostaDTO;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoExcecao.class)
    public ResponseEntity<ErroRespostaDTO> tratarNaoEncontrado(
            RecursoNaoEncontradoExcecao excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                excecao.getMessage(),
                requisicao
        );
    }

    @ExceptionHandler(RegraDeNegocioExcecao.class)
    public ResponseEntity<ErroRespostaDTO> tratarRegraDeNegocio(
            RegraDeNegocioExcecao excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                excecao.getMessage(),
                requisicao
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> tratarValidacao(
            MethodArgumentNotValidException excecao,
            HttpServletRequest requisicao
    ) {
        String mensagem = excecao.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Dados enviados são inválidos.");

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                mensagem,
                requisicao
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroRespostaDTO> tratarArgumentoInvalido(
            IllegalArgumentException excecao,
            HttpServletRequest requisicao
    ) {
        String mensagem = excecao.getMessage() != null
                ? excecao.getMessage()
                : "Dados enviados são inválidos.";

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                mensagem,
                requisicao
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroRespostaDTO> tratarArquivoGrande(
            MaxUploadSizeExceededException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Arquivo grande demais para o servidor.",
                requisicao
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> tratarErroInterno(
            Exception excecao,
            HttpServletRequest requisicao
    ) {
        log.error(
                "Erro interno ao processar {} {}",
                requisicao.getMethod(),
                requisicao.getRequestURI(),
                excecao
        );

        return criarResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor.",
                requisicao
        );
    }

    private ResponseEntity<ErroRespostaDTO> criarResposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest requisicao
    ) {
        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                requisicao.getRequestURI()
        );

        return ResponseEntity.status(status).body(resposta);
    }
}