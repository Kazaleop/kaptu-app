
package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.excecao.RecursoNaoEncontradoExcecao;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import br.fmc.kaptu.domain.imovel.Imovel;
import br.fmc.kaptu.domain.imovel.StatusOcupacao;
import br.fmc.kaptu.service.ImovelService;
import br.fmc.kaptu.service.LocatarioService;
import br.fmc.kaptu.service.ContratoService;
import br.fmc.kaptu.web.handler.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;

import java.math.BigDecimal;

class ImobiliarioControllerTest {

    private MockMvc mockMvc;

    private ImovelService imovelService;
    private LocatarioService locatarioService;
    private ContratoService contratoService;

    @BeforeEach
    void configurar() {

        imovelService = Mockito.mock(ImovelService.class);
        locatarioService = Mockito.mock(LocatarioService.class);
        contratoService = Mockito.mock(ContratoService.class);

        ImobiliarioController controller = new ImobiliarioController(
                imovelService,
                locatarioService,
                contratoService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveCadastrarImovelComSucesso() throws Exception {

        Imovel imovel = Mockito.mock(Imovel.class);

        when(imovelService.cadastrar(
                "Apartamento 101",
                "Rua A, 100",
                "Centro",
                4,
                new BigDecimal("1500.00")
        )).thenReturn(imovel);

        when(imovel.getId()).thenReturn(1L);
        when(imovel.getNome()).thenReturn("Apartamento 101");
        when(imovel.getEndereco()).thenReturn("Rua A, 100");
        when(imovel.getBairro()).thenReturn("Centro");
        when(imovel.getQuantResidentes()).thenReturn(4);
        when(imovel.getValorAluguelMensal())
                .thenReturn(new BigDecimal("1500.00"));
        when(imovel.getStatusOcupacao())
                .thenReturn(StatusOcupacao.DISPONIVEL);

        mockMvc.perform(post("/api/v1/imoveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Apartamento 101",
                                  "endereco": "Rua A, 100",
                                  "bairro": "Centro",
                                  "quantResidentes": 4,
                                  "valorAluguelMensal": 1500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Apartamento 101"))
                .andExpect(jsonPath("$.statusOcupacao").value("DISPONIVEL"));
    }

    @Test
    void deveRetornar404QuandoImovelNaoExiste() throws Exception {

        when(imovelService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoExcecao(
                        "Imóvel não encontrado."
                ));

        mockMvc.perform(get("/api/v1/imoveis/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem")
                        .value("Imóvel não encontrado."));
    }

    @Test
    void deveRetornar400QuandoRegraDeNegocioForViolada() throws Exception {

        when(imovelService.cadastrar(
                "Apartamento 101",
                "Rua A, 100",
                "Centro",
                4,
                new BigDecimal("1500.00")
        )).thenThrow(new RegraDeNegocioExcecao(
                "Este imóvel já está cadastrado."
        ));

        mockMvc.perform(post("/api/v1/imoveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Apartamento 101",
                                  "endereco": "Rua A, 100",
                                  "bairro": "Centro",
                                  "quantResidentes": 4,
                                  "valorAluguelMensal": 1500.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem")
                        .value("Este imóvel já está cadastrado."));
    }

    @Test
    void deveRetornarListaDeImoveis() throws Exception {

        when(imovelService.listar()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/imoveis"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(imovelService).listar();
    }
}
