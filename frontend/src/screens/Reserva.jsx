import { useEffect, useState } from "react";
import { api } from "../api";

const PASOS = ["Especial.", "Médico", "Horario", "Confirm."];

export default function Reserva({ volver }) {
  const [paso, setPaso] = useState(1);
  const [especialidades, setEspecialidades] = useState([]);
  const [horariosEsp, setHorariosEsp] = useState([]); // para calcular estados
  const [busqueda, setBusqueda] = useState("");
  const [especialidad, setEspecialidad] = useState(null);
  const [medicos, setMedicos] = useState([]);
  const [medico, setMedico] = useState(null);
  const [horarios, setHorarios] = useState([]);
  const [fechaSel, setFechaSel] = useState(null);
  const [horarioSel, setHorarioSel] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.especialidades().then(setEspecialidades).catch(() => {});
    api.horarios().then(setHorariosEsp).catch(() => {});
  }, []);

  function estadoEspecialidad(esp) {
    const hs = horariosEsp.filter((h) => h.especialidad === esp && h.cuposDisponibles > 0);
    if (hs.length === 0) return { txt: "Lista de espera", color: "#dc2626" };
    const hoy = new Date().toISOString().slice(0, 10);
    if (hs.some((h) => h.fecha === hoy)) return { txt: "Disponible hoy", color: "#16a34a" };
    const prox = hs.map((h) => h.fecha).sort()[0];
    return { txt: `Próximo: ${prox}`, color: "#0072bc" };
  }

  function elegirEspecialidad(esp) {
    setEspecialidad(esp); setError(null);
    api.medicos(esp).then((ms) => { setMedicos(ms); setPaso(2); }).catch((e) => setError(e.message));
  }

  function elegirMedico(m) {
    setMedico(m); setError(null);
    api.horariosMedico(m.idMedico).then((hs) => {
      const disp = hs.filter((h) => h.cuposDisponibles > 0);
      setHorarios(disp);
      setFechaSel(disp[0]?.fecha || null);
      setPaso(3);
    }).catch((e) => setError(e.message));
  }

  const fechas = [...new Set(horarios.map((h) => h.fecha))].sort();
  const slots = horarios.filter((h) => h.fecha === fechaSel);

  async function confirmar() {
    setError(null);
    try {
      const cita = await api.reservar(horarioSel.idHorario);
      setResultado(cita); setPaso(4);
    } catch (e) {
      if (e.message.includes("cupos")) {
        if (confirm("No hay cupos. ¿Unirte a la lista de espera?")) {
          try { const r = await api.listaEspera(horarioSel.idHorario);
            setResultado({ listaEspera: true, posicion: r.posicion }); setPaso(4);
          } catch (e2) { setError(e2.message); }
        }
      } else setError(e.message);
    }
  }

  const especialidadesFiltradas = especialidades.filter((e) =>
    e.toLowerCase().includes(busqueda.toLowerCase()));

  return (
    <div className="screen">
      <div className="topbar">
        <button className="topbar-back" onClick={volver}>‹</button>
        <div>
          <div className="topbar-title">CitaFácil EsSalud</div>
          <div className="topbar-sub">Reservar cita</div>
        </div>
      </div>

      {/* Barra de pasos */}
      <div className="pasos">
        {PASOS.map((p, i) => (
          <div className="paso" key={p}>
            <div className={`paso-num ${paso >= i + 1 ? "activo" : ""}`}>{i + 1}</div>
            <span>{p}</span>
          </div>
        ))}
      </div>

      <div className="reserva-body">
        {error && <p className="error">⛔ {error}</p>}

        {paso === 1 && (
          <>
            <h3 className="seccion">Selecciona una especialidad</h3>
            <input className="buscador" placeholder="Buscar especialidad…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
            <div className="lista-esp">
              {especialidadesFiltradas.map((esp) => {
                const est = estadoEspecialidad(esp);
                return (
                  <button className="item-esp" key={esp} onClick={() => elegirEspecialidad(esp)}>
                    <div>
                      <strong>{esp}</strong>
                      <span className="estado-dot" style={{ color: est.color }}>● {est.txt}</span>
                    </div>
                    <span className="chevron">›</span>
                  </button>
                );
              })}
            </div>
          </>
        )}

        {paso === 2 && (
          <>
            <h3 className="seccion">Elige un médico · {especialidad}</h3>
            <div className="lista-esp">
              {medicos.map((m) => (
                <button className="item-esp" key={m.idMedico} onClick={() => elegirMedico(m)}>
                  <div className="med-row">
                    <span className="avatar">DR</span>
                    <div>
                      <strong>Dr(a). {m.nombres} {m.apellidos}</strong>
                      <span className="estado-dot">{m.especialidad}</span>
                    </div>
                  </div>
                  <span className="chevron">›</span>
                </button>
              ))}
              {medicos.length === 0 && <div className="noti vacia">No hay médicos en esta especialidad</div>}
            </div>
            <button className="btn-outline" onClick={() => setPaso(1)}>‹ Volver a especialidades</button>
          </>
        )}

        {paso === 3 && (
          <>
            <div className="med-card">
              <span className="avatar grande">DR</span>
              <div>
                <strong>Dr(a). {medico.nombres} {medico.apellidos}</strong>
                <div className="med-sub">{medico.especialidad}</div>
                <div className="med-sub azul">{medico.establecimiento}</div>
              </div>
            </div>

            <h3 className="seccion">Selecciona el día</h3>
            <div className="fechas">
              {fechas.map((f) => (
                <button key={f} className={`fecha-chip ${f === fechaSel ? "activo" : ""}`} onClick={() => { setFechaSel(f); setHorarioSel(null); }}>
                  {f.slice(8, 10)}/{f.slice(5, 7)}
                </button>
              ))}
            </div>

            <h3 className="seccion">Horarios disponibles</h3>
            <div className="slots">
              {slots.map((h) => (
                <button key={h.idHorario}
                  className={`slot ${horarioSel?.idHorario === h.idHorario ? "activo" : ""}`}
                  onClick={() => setHorarioSel(h)}>
                  {h.horaInicio?.slice(0, 5)}
                </button>
              ))}
              {slots.length === 0 && <div className="noti vacia">Sin horarios este día</div>}
            </div>

            <button className="btn-primario" disabled={!horarioSel} onClick={confirmar}>CONFIRMAR CITA</button>
            <button className="btn-outline" onClick={() => setPaso(2)}>‹ Volver a médicos</button>
          </>
        )}

        {paso === 4 && (
          <div className="confirmacion">
            {resultado?.listaEspera ? (
              <>
                <div className="check naranja">📋</div>
                <h2>Te uniste a la lista de espera</h2>
                <p>Posición #{resultado.posicion}. Te avisaremos por WhatsApp si se libera un cupo.</p>
              </>
            ) : (
              <>
                <div className="check">✓</div>
                <h2>¡Cita confirmada!</h2>
                <div className="resumen">
                  <div><span>Código</span><strong>{resultado.codigoReserva}</strong></div>
                  <div><span>Especialidad</span><strong>{resultado.especialidad}</strong></div>
                  <div><span>Médico</span><strong>{resultado.medico}</strong></div>
                  <div><span>Fecha</span><strong>{resultado.fecha} {resultado.horaInicio?.slice(0,5)}</strong></div>
                </div>
                <p className="mini">📲 Te enviamos la confirmación por WhatsApp.</p>
              </>
            )}
            <button className="btn-primario" onClick={volver}>Volver al inicio</button>
          </div>
        )}
      </div>
    </div>
  );
}
