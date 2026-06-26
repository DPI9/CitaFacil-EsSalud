package pe.utp.citafacil.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.utp.citafacil.model.Establecimiento;
import pe.utp.citafacil.model.Medico;
import pe.utp.citafacil.repository.EstablecimientoRepository;
import pe.utp.citafacil.repository.MedicoRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoController {

    private final EstablecimientoRepository establecimientoRepo;
    private final MedicoRepository medicoRepo;

    @GetMapping("/establecimientos")
    public List<Establecimiento> establecimientos() {
        return establecimientoRepo.findAll();
    }

    @PostMapping("/establecimientos")
    public Establecimiento crearEstablecimiento(@RequestBody Establecimiento e) {
        return establecimientoRepo.save(e);
    }

    @GetMapping("/medicos")
    public List<Medico> medicos(@RequestParam(required = false) String especialidad) {
        if (especialidad != null && !especialidad.isBlank()) {
            return medicoRepo.findByEspecialidadIgnoreCase(especialidad);
        }
        return medicoRepo.findAll();
    }

    @GetMapping("/especialidades")
    public List<String> especialidades() {
        return medicoRepo.findEspecialidades();
    }
}
