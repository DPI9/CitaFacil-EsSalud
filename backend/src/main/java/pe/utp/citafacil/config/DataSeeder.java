package pe.utp.citafacil.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.utp.citafacil.model.*;
import pe.utp.citafacil.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos de ejemplo la primera vez que arranca el sistema:
 * establecimientos, medicos, horarios, y citas/notificaciones de muestra
 * para que el Home y el Dashboard administrativo se vean poblados.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EstablecimientoRepository establecimientoRepo;
    private final MedicoRepository medicoRepo;
    private final HorarioDisponibleRepository horarioRepo;
    private final AseguradoRepository aseguradoRepo;
    private final CitaRepository citaRepo;
    private final NotificacionRepository notificacionRepo;
    private final PasswordEncoder passwordEncoder;

    private int codigoSeq = 1000;

    @Override
    public void run(String... args) {
        if (establecimientoRepo.count() > 0) return; // ya sembrado

        Establecimiento rebagliati = establecimientoRepo.save(Establecimiento.builder()
            .nombre("Hospital Nacional Edgardo Rebagliati Martins")
            .direccion("Av. Rebagliati 490").distrito("Jesus Maria").tipo("HOSPITAL").build());
        Establecimiento almenara = establecimientoRepo.save(Establecimiento.builder()
            .nombre("Hospital Nacional Guillermo Almenara Irigoyen")
            .direccion("Av. Grau 800").distrito("La Victoria").tipo("HOSPITAL").build());

        List<Medico> medicos = List.of(
            medicoRepo.save(Medico.builder().nombres("Ana").apellidos("Torres Rojas")
                .especialidad("Traumatologia").cmp("CMP-12345").establecimiento(rebagliati).build()),
            medicoRepo.save(Medico.builder().nombres("Luis").apellidos("Mendoza Diaz")
                .especialidad("Endocrinologia").cmp("CMP-23456").establecimiento(rebagliati).build()),
            medicoRepo.save(Medico.builder().nombres("Carla").apellidos("Quispe Vega")
                .especialidad("Cardiologia").cmp("CMP-34567").establecimiento(almenara).build()),
            medicoRepo.save(Medico.builder().nombres("Jorge").apellidos("Ramos Leon")
                .especialidad("Medicina General").cmp("CMP-45678").establecimiento(almenara).build()),
            medicoRepo.save(Medico.builder().nombres("Sofia").apellidos("Castro Pinto")
                .especialidad("Dermatologia").cmp("CMP-56789").establecimiento(rebagliati).build()),
            medicoRepo.save(Medico.builder().nombres("Pedro").apellidos("Salas Muro")
                .especialidad("Pediatria").cmp("CMP-67890").establecimiento(almenara).build())
        );

        // Horarios para hoy y proximos 3 dias, en varias horas
        int[] horas = {8, 10, 12, 15, 17};
        LocalDate hoy = LocalDate.now();
        List<HorarioDisponible> horariosHoy = new ArrayList<>();
        for (Medico m : medicos) {
            for (int dia = 0; dia <= 3; dia++) {
                for (int h : horas) {
                    HorarioDisponible hor = horarioRepo.save(HorarioDisponible.builder()
                            .medico(m).fecha(hoy.plusDays(dia))
                            .horaInicio(LocalTime.of(h, 0)).horaFin(LocalTime.of(h + 1, 0))
                            .cuposTotales(5).cuposDisponibles(5).estado("DISPONIBLE").build());
                    if (dia == 0) horariosHoy.add(hor);
                }
            }
        }

        // Asegurados de ejemplo (clave: 123456)
        String hash = passwordEncoder.encode("123456");
        List<Asegurado> asegurados = new ArrayList<>();
        String[][] datos = {
            {"40111222", "Maria", "Perez Jimenez"}, {"40222333", "Carlos", "Soto Marin"},
            {"40333444", "Lucia", "Ramos Lopez"},   {"40444555", "Diego", "Vega Cruz"},
            {"40555666", "Elena", "Mejia Diaz"},     {"40666777", "Raul", "Quispe Soto"},
            {"40777888", "Nora", "Flores Rios"},     {"40888999", "Hugo", "Diaz Pena"}
        };
        for (String[] d : datos) {
            asegurados.add(aseguradoRepo.save(Asegurado.builder()
                .dni(d[0]).nombres(d[1]).apellidos(d[2])
                .telefono("9" + d[0].substring(1)).correo(d[1].toLowerCase() + "@correo.com")
                .contrasenaHash(hash).build()));
        }

        // Citas de muestra distribuidas en las horas de hoy
        // Estados: ATENDIDA / NO_ASISTIO (para tasa asistencia), CONFIRMADA, CANCELADA
        int i = 0;
        for (HorarioDisponible hor : horariosHoy) {
            Asegurado a = asegurados.get(i % asegurados.size());
            EstadoCita estado;
            int r = i % 10;
            if (r < 6) estado = EstadoCita.ATENDIDA;       // 60%
            else if (r < 7) estado = EstadoCita.NO_ASISTIO; // 10%
            else if (r < 9) estado = EstadoCita.CONFIRMADA; // 20%
            else estado = EstadoCita.CANCELADA;             // 10%

            crearCita(a, hor, estado);
            i++;
            if (i >= 30) break; // suficientes para poblar el dashboard
        }

        System.out.println(">> Datos de ejemplo cargados: 2 establecimientos, 6 medicos, horarios (4 dias) y citas de muestra.");
        System.out.println(">> Usuarios demo (clave 123456): 40111222, 40222333, ... (o registra tu propio DNI).");
    }

    private void crearCita(Asegurado a, HorarioDisponible hor, EstadoCita estado) {
        Cita c = Cita.builder()
                .asegurado(a).horario(hor)
                .codigoReserva("CF-" + (codigoSeq++))
                .estado(estado)
                .canalReserva("WEB")
                .fechaReserva(LocalDateTime.now())
                .build();
        if (estado == EstadoCita.CANCELADA) {
            c.setFechaCancelacion(LocalDateTime.now());
        } else {
            // ocupa un cupo
            hor.setCuposDisponibles(Math.max(0, hor.getCuposDisponibles() - 1));
            horarioRepo.save(hor);
        }
        c = citaRepo.save(c);

        String tipo = switch (estado) {
            case CONFIRMADA -> "CONFIRMACION";
            case CANCELADA -> "CANCELACION";
            default -> "RECORDATORIO";
        };
        notificacionRepo.save(Notificacion.builder()
                .cita(c).tipo(tipo).canal("WHATSAPP")
                .fechaEnvio(LocalDateTime.now()).estadoEnvio("ENVIADO").build());
    }
}
