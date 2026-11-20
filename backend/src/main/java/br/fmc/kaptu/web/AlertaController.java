
package br.fmc.kaptu.web;

import br.fmc.kaptu.domain.alerta.AlertaInteligente;
import br.fmc.kaptu.service.AlertaService;
import br.fmc.kaptu.web.dto.AlertaDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<AlertaDTO> listarTodos() {
        return alertaService.listarTodos()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/nao-lidos")
    public List<AlertaDTO> listarNaoLidos() {
        return alertaService.listarNaoLidos()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PatchMapping("/{id}/lido")
    public void marcarComoLido(@PathVariable Long id) {
        alertaService.marcarComoLido(id);
    }

    private AlertaDTO toDTO(AlertaInteligente alerta) {
        return new AlertaDTO(
                alerta.getId(),
                alerta.getTipoAlerta(),
                alerta.getMensagem(),
                alerta.getReferenciaId(),
                alerta.getDataGeracao(),
                alerta.isLido()
        );
    }
}
