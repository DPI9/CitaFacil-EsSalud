package pe.utp.citafacil.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "sistema", "CitaFacil EsSalud",
            "estado", "OK",
            "mensaje", "Backend operativo",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
