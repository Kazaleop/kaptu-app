package br.fmc.kaptu.web.handler;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class ControladorDeTeste {
        @GetMapping("/api/v1/teste/413")
        public void lancar413() {
            throw new MaxUploadSizeExceededException(1024);
        }

        @GetMapping("/api/v1/teste/enum")
        public void enumParam(
                @org.springframework.web.bind.annotation.RequestParam
                br.fmc.kaptu.domain.documento.TipoDocumento tipo) {
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ControladorDeTeste())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void deveRetornar404ParaRotaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/rota-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem")
                        .value("Recurso não encontrado."));
    }

    @Test
    void deveRetornar400ParaEnumInvalidoNaQuery() throws Exception {
        mockMvc.perform(get("/api/v1/teste/enum")
                        .param("tipo", "NAO_EXISTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveRetornar413ParaArquivoGrande() throws Exception {
        mockMvc.perform(get("/api/v1/teste/413"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.mensagem")
                        .value("Arquivo maior que o limite de 10 MB."));
    }
}
