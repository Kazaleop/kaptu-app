
package br.fmc.kaptu.web;

import br.fmc.kaptu.service.DashboardService;
import br.fmc.kaptu.web.dto.DashboardDTO;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public DashboardDTO resumo() {
        return dashboardService.gerarResumo();
    }
}
