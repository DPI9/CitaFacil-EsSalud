package pe.utp.citafacil.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.utp.citafacil.dto.HorarioResponse;
import pe.utp.citafacil.repository.HorarioDisponibleRepository;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioDisponibleRepository horarioRepository;

    /** Lista horarios con cupos; si se pasa especialidad, filtra por ella. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<HorarioResponse> listar(@RequestParam(required = false) String especialidad) {
        var lista = (especialidad != null && !especialidad.isBlank())
                ? horarioRepository.findByMedico_EspecialidadIgnoreCaseAndCuposDisponiblesGreaterThan(especialidad, 0)
                : horarioRepository.findByCuposDisponiblesGreaterThan(0);
        return lista.stream().map(HorarioResponse::de).toList();
    }

    @GetMapping("/medico/{idMedico}")
    @Transactional(readOnly = true)
    public List<HorarioResponse> porMedico(@PathVariable Long idMedico) {
        return horarioRepository.findByMedico_IdMedico(idMedico)
                .stream().map(HorarioResponse::de).toList();
    }
}
