
package br.fmc.kaptu.job;

import br.fmc.kaptu.service.AlertaService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertaJob {

    private final AlertaService alertaService;

    public AlertaJob(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "America/Manaus")
    public void verificarVencimentosDiariamente() {
        alertaService.verificarVencimentos();
    }
}
