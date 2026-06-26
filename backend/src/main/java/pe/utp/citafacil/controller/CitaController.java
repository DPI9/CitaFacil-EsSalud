package pe.utp.citafacil.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.utp.citafacil.dto.CitaResponse;
import pe.utp.citafacil.dto.ReservaRequest;
import pe.utp.citafacil.model.ListaEspera;
import pe.utp.citafacil.service.CitaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public CitaResponse reservar(@AuthenticationPrincipal UserDetails user,
                                 @Valid @RequestBody ReservaRequest req) {
        return citaService.reservar(user.getUsername(), req.idHorario(), req.canal());
    }

    @GetMapping("/mias")
    public List<CitaResponse> misCitas(@AuthenticationPrincipal UserDetails user) {
        return citaService.misCitas(user.getUsername());
    }

    @DeleteMapping("/{id}")
    public CitaResponse cancelar(@AuthenticationPrincipal UserDetails user,
                                 @PathVariable Long id) {
        return citaService.cancelar(user.getUsername(), id);
    }

    @PostMapping("/lista-espera/{idHorario}")
    public Map<String, Object> unirseListaEspera(@AuthenticationPrincipal UserDetails user,
                                                 @PathVariable Long idHorario) {
        ListaEspera e = citaService.unirseListaEspera(user.getUsername(), idHorario);
        return Map.of(
                "mensaje", "Te has unido a la lista de espera",
                "posicion", e.getPosicion(),
                "idEspera", e.getIdEspera()
        );
    }
}
