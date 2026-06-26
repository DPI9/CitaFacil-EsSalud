package pe.utp.citafacil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.citafacil.model.Cita;
import pe.utp.citafacil.model.EstadoCita;
import pe.utp.citafacil.model.HorarioDisponible;
import pe.utp.citafacil.repository.AseguradoRepository;
import pe.utp.citafacil.repository.CitaRepository;
import pe.utp.citafacil.repository.HorarioDisponibleRepository;
import pe.utp.citafacil.repository.ListaEsperaRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CitaRepository citaRepository;
    private final ListaEsperaRepository listaEsperaRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final AseguradoRepository aseguradoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> kpis() {
        List<Cita> citas = citaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        long citasHoy = citas.stream()
                .filter(c -> c.getHorario() != null && hoy.equals(c.getHorario().getFecha()))
                .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA || c.getEstado() == EstadoCita.ATENDIDA)
                .count();

        long cancelacionesHoy = citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
                .filter(c -> c.getFechaCancelacion() != null && hoy.equals(c.getFechaCancelacion().toLocalDate()))
                .count();

        long enListaEspera = listaEsperaRepository.findAll().stream()
                .filter(e -> "EN_ESPERA".equals(e.getEstadoEspera()))
                .count();

        long atendidas = citas.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
        long noAsistio = citas.stream().filter(c -> c.getEstado() == EstadoCita.NO_ASISTIO).count();
        long baseAsist = atendidas + noAsistio;
        int tasaAsistencia = baseAsist == 0 ? 0 : (int) Math.round(atendidas * 100.0 / baseAsist);

        // Ocupacion por hora (08:00 - 19:00)
        List<Map<String, Object>> ocupacion = new ArrayList<>();
        Map<Integer, Long> porHora = new HashMap<>();
        for (Cita c : citas) {
            if (c.getHorario() != null && c.getHorario().getHoraInicio() != null
                    && c.getEstado() != EstadoCita.CANCELADA) {
                int h = c.getHorario().getHoraInicio().getHour();
                porHora.merge(h, 1L, Long::sum);
            }
        }
        for (int h = 8; h <= 19; h++) {
            Map<String, Object> punto = new LinkedHashMap<>();
            punto.put("hora", String.format("%02d:00", h));
            punto.put("cantidad", porHora.getOrDefault(h, 0L));
            ocupacion.add(punto);
        }

        // Proximas citas confirmadas
        List<Map<String, Object>> proximas = citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA && c.getHorario() != null)
                .sorted(Comparator.comparing((Cita c) -> c.getHorario().getFecha())
                        .thenComparing(c -> c.getHorario().getHoraInicio()))
                .limit(6)
                .map(c -> {
                    var m = c.getHorario().getMedico();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("hora", c.getHorario().getHoraInicio() != null ? c.getHorario().getHoraInicio().toString() : "");
                    map.put("paciente", c.getAsegurado().getApellidos());
                    map.put("especialidad", m != null ? m.getEspecialidad() : "");
                    map.put("estado", c.getEstado().name());
                    return map;
                }).toList();

        // Alertas operativas: especialidades saturadas (horarios sin cupos)
        List<Map<String, Object>> alertas = new ArrayList<>();
        Map<String, Boolean> saturadaPorEsp = new LinkedHashMap<>();
        for (HorarioDisponible h : horarioRepository.findAll()) {
            if (h.getMedico() != null) {
                String esp = h.getMedico().getEspecialidad();
                boolean saturado = h.getCuposDisponibles() != null && h.getCuposDisponibles() == 0;
                saturadaPorEsp.merge(esp, saturado, (a, b) -> a || b);
            }
        }
        saturadaPorEsp.forEach((esp, sat) -> {
            if (sat) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("nivel", "alto");
                a.put("titulo", "Saturación en " + esp);
                a.put("detalle", "Hay horarios sin cupos disponibles");
                alertas.add(a);
            }
        });
        if (cancelacionesHoy >= 3) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("nivel", "medio");
            a.put("titulo", cancelacionesHoy + " cancelaciones hoy");
            a.put("detalle", "Lista de espera notificada automáticamente");
            alertas.add(a);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("citasHoy", citasHoy);
        resp.put("cancelacionesHoy", cancelacionesHoy);
        resp.put("enListaEspera", enListaEspera);
        resp.put("tasaAsistencia", tasaAsistencia);
        resp.put("ocupacionPorHora", ocupacion);
        resp.put("proximasCitas", proximas);
        resp.put("alertas", alertas);
        return resp;
    }

    /** Lista de citas para el panel; filtra por estado y/o "hoy". */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> citas(String estado, boolean soloHoy) {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findAll().stream()
                .filter(c -> c.getHorario() != null)
                .filter(c -> estado == null || estado.isBlank() || (c.getEstado() != null && c.getEstado().name().equalsIgnoreCase(estado)))
                .filter(c -> !soloHoy || hoy.equals(c.getHorario().getFecha()))
                .sorted(Comparator.comparing((Cita c) -> c.getHorario().getFecha())
                        .thenComparing(c -> c.getHorario().getHoraInicio()))
                .map(this::citaMap)
                .toList();
    }

    private Map<String, Object> citaMap(Cita c) {
        var h = c.getHorario();
        var m = h != null ? h.getMedico() : null;
        var est = m != null ? m.getEstablecimiento() : null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("codigo", c.getCodigoReserva());
        map.put("paciente", c.getAsegurado().getNombres() + " " + c.getAsegurado().getApellidos());
        map.put("dni", c.getAsegurado().getDni());
        map.put("especialidad", m != null ? m.getEspecialidad() : "");
        map.put("medico", m != null ? ("Dr(a). " + m.getNombres() + " " + m.getApellidos()) : "");
        map.put("establecimiento", est != null ? est.getNombre() : "");
        map.put("fecha", h != null ? h.getFecha().toString() : "");
        map.put("hora", h != null && h.getHoraInicio() != null ? h.getHoraInicio().toString() : "");
        map.put("estado", c.getEstado() != null ? c.getEstado().name() : "");
        return map;
    }

    /** Lista de espera con detalle, para el panel. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listaEspera() {
        return listaEsperaRepository.findAll().stream().map(e -> {
            var h = e.getHorario();
            var m = h != null ? h.getMedico() : null;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("posicion", e.getPosicion());
            map.put("paciente", e.getAsegurado().getNombres() + " " + e.getAsegurado().getApellidos());
            map.put("dni", e.getAsegurado().getDni());
            map.put("especialidad", m != null ? m.getEspecialidad() : "");
            map.put("medico", m != null ? ("Dr(a). " + m.getNombres() + " " + m.getApellidos()) : "");
            map.put("fecha", h != null ? h.getFecha().toString() : "");
            map.put("hora", h != null && h.getHoraInicio() != null ? h.getHoraInicio().toString() : "");
            map.put("estado", e.getEstadoEspera());
            return map;
        }).toList();
    }

    /** Lista de asegurados para que el personal seleccione al paciente. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> asegurados() {
        return aseguradoRepository.findAll().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAsegurado", a.getIdAsegurado());
            m.put("dni", a.getDni());
            m.put("nombre", a.getNombres() + " " + a.getApellidos());
            return m;
        }).toList();
    }

    /** Indicadores resumidos para la seccion de Reportes. */
    @Transactional(readOnly = true)
    public Map<String, Object> reportes() {
        List<Cita> citas = citaRepository.findAll();
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoCita e : EstadoCita.values()) {
            porEstado.put(e.name(), citas.stream().filter(c -> c.getEstado() == e).count());
        }
        Map<String, Long> porEspecialidad = new LinkedHashMap<>();
        for (Cita c : citas) {
            if (c.getHorario() != null && c.getHorario().getMedico() != null) {
                porEspecialidad.merge(c.getHorario().getMedico().getEspecialidad(), 1L, Long::sum);
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("totalCitas", citas.size());
        resp.put("porEstado", porEstado);
        resp.put("porEspecialidad", porEspecialidad);
        return resp;
    }
}
