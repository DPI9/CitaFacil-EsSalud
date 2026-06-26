package pe.utp.citafacil.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.utp.citafacil.dto.AdminReservaRequest;
import pe.utp.citafacil.dto.CitaResponse;
import pe.utp.citafacil.service.AdminService;
import pe.utp.citafacil.service.CitaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CitaService citaService;

    @GetMapping("/kpis")
    public Map<String, Object> kpis() {
        return adminService.kpis();
    }

    @GetMapping("/citas")
    public List<Map<String, Object>> citas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false, defaultValue = "false") boolean hoy) {
        return adminService.citas(estado, hoy);
    }

    @GetMapping("/lista-espera")
    public List<Map<String, Object>> listaEspera() {
        return adminService.listaEspera();
    }

    @GetMapping("/reportes")
    public Map<String, Object> reportes() {
        return adminService.reportes();
    }

    @GetMapping("/asegurados")
    public List<Map<String, Object>> asegurados() {
        return adminService.asegurados();
    }

    /** Reserva una cita a nombre de un paciente (modulo recepcionista). */
    @PostMapping("/reservar")
    public CitaResponse reservar(@Valid @RequestBody AdminReservaRequest req) {
        return citaService.reservar(req.dni(), req.idHorario(), "ADMIN");
    }
}
