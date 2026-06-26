package pe.utp.citafacil.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.utp.citafacil.model.Asegurado;
import pe.utp.citafacil.repository.AseguradoRepository;

@Service
@RequiredArgsConstructor
public class AseguradoDetailsService implements UserDetailsService {

    private final AseguradoRepository aseguradoRepository;

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Asegurado a = aseguradoRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Asegurado no encontrado: " + dni));
        return User.builder()
                .username(a.getDni())
                .password(a.getContrasenaHash())
                .roles("ASEGURADO")
                .build();
    }
}
