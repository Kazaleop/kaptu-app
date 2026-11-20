package br.fmc.kaptu.web.handler;

import br.fmc.kaptu.domain.excecao.AcessoNegadoExcecao;
import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.web.dto.ErroRespostaDTO;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
                "Arquivo maior que o limite de 10 MB.",
                requisicao
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroRespostaDTO> tratarNaoEncontradoRota(
            NoResourceFoundException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado.",
                requisicao
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroRespostaDTO> tratarMetodoNaoPermitido(
            HttpRequestMethodNotSupportedException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método não permitido.",
                requisicao
        );
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class })
    public ResponseEntity<ErroRespostaDTO> tratarParametroAusente(
            Exception excecao,
            HttpServletRequest requisicao
    ) {
        String nome = null;

        if (excecao instanceof MissingServletRequestParameterException p) {
            nome = p.getParameterName();
        } else if (excecao instanceof MissingServletRequestPartException p) {
            nome = p.getRequestPartName();
        }

        String mensagem = "Parâmetro obrigatório ausente: "
                + (nome != null ? nome : "");

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                mensagem.trim(),
                requisicao
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroRespostaDTO> tratarTipoInvalido(
            MethodArgumentTypeMismatchException excecao,
            HttpServletRequest requisicao
    ) {
        String nome = excecao.getName() != null
                ? excecao.getName()
                : "parâmetro";

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro " + nome + ".",
                requisicao
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroRespostaDTO> tratarCorpoInvalido(
            HttpMessageNotReadableException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido.",
                requisicao
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroRespostaDTO> tratarIntegridade(
            DataIntegrityViolationException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                "Registro duplicado ou em uso.",
                requisicao
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErroRespostaDTO> tratarRotaInexistente(
            NoHandlerFoundException excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado.",
                requisicao
        );
    }

    @ExceptionHandler(AcessoNegadoExcecao.class)
    public ResponseEntity<ErroRespostaDTO> tratarAcessoNegado(
            AcessoNegadoExcecao excecao,
            HttpServletRequest requisicao
    ) {
        return criarResposta(
                HttpStatus.FORBIDDEN,
                "Você não tem permissão para esta ação.",
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