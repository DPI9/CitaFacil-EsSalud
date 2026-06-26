package pe.utp.citafacil.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.utp.citafacil.dto.NotificacionResponse;
import pe.utp.citafacil.repository.AseguradoRepository;
import pe.utp.citafacil.repository.NotificacionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;
    private final AseguradoRepository aseguradoRepository;

    @GetMapping("/mias")
    @Transactional(readOnly = true)
    public List<NotificacionResponse> mias(@AuthenticationPrincipal UserDetails user) {
        var asegurado = aseguradoRepository.findByDni(user.getUsername()).orElseThrow();
        return notificacionRepository
                .findByCita_Asegurado_IdAseguradoOrderByFechaEnvioDesc(asegurado.getIdAsegurado())
                .stream().map(NotificacionResponse::de).toList();
    }
}
