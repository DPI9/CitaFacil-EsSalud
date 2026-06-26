package pe.utp.citafacil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.utp.citafacil.dto.AuthResponse;
import pe.utp.citafacil.dto.LoginRequest;
import pe.utp.citafacil.dto.RegistroRequest;
import pe.utp.citafacil.model.Asegurado;
import pe.utp.citafacil.repository.AseguradoRepository;
import pe.utp.citafacil.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AseguradoRepository aseguradoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registrar(RegistroRequest req) {
        if (aseguradoRepository.existsByDni(req.dni())) {
            throw new IllegalArgumentException("Ya existe un asegurado con ese DNI");
        }
        Asegurado a = Asegurado.builder()
                .dni(req.dni())
                .nombres(req.nombres())
                .apellidos(req.apellidos())
                .telefono(req.telefono())
                .correo(req.correo())
                .contrasenaHash(passwordEncoder.encode(req.contrasena()))
                .build();
        a = aseguradoRepository.save(a);
        String token = jwtService.generarToken(a.getDni());
        return new AuthResponse(token, a.getIdAsegurado(), a.getDni(), a.getNombres(), a.getApellidos());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.dni(), req.contrasena()));
        Asegurado a = aseguradoRepository.findByDni(req.dni()).orElseThrow();
        String token = jwtService.generarToken(a.getDni());
        return new AuthResponse(token, a.getIdAsegurado(), a.getDni(), a.getNombres(), a.getApellidos());
    }
}
