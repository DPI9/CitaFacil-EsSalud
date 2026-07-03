import { useEffect, useState } from "react";
import { api } from "../api";

export default function MisCitas() {
  const [citas, setCitas] = useState([]);
  const [error, setError] = useState(null);
  const [msg, setMsg] = useState(null);
  const [reprog, setReprog] = useState(null); // cita en reprogramacion
  const [horarios, setHorarios] = useState([]);

  function cargar() {
    api.misCitas().then(setCitas).catch((e) => setError(e.message));
  }
  useEffect(cargar, []);

  async function cancelar(id) {
    if (!confirm("¿Cancelar esta cita?")) return;
    setError(null); setMsg(null);
    try { await api.cancelar(id); setMsg("Cita cancelada."); cargar(); }
    catch (e) { setError(e.message); }
  }

  function abrirReprog(c) {
    setError(null); setMsg(null); setReprog(c); setHorarios([]);
    api.horarios(c.especialidad).then((hs) =>
      setHorarios(hs.filter((h) => h.cuposDisponibles > 0))).catch(() => {});
  }

  async function confirmarReprog(idHorario) {
    setError(null);
    try {
      await api.reprogramar(reprog.idCita, idHorario);
      setMsg("✅ Cita reprogramada.");
      setReprog(null); cargar();
    } catch (e) { setError(e.message); }
  }

  return (
    <div className="screen">
      <div className="topbar">
        <div className="topbar-title">CitaFácil EsSalud</div>
        <div className="topbar-sub">Mis citas</div>
      </div>
      <div className="home-body">
        {error && <p className="error">⛔ {error}</p>}
        {msg && <p className="ok">{msg}</p>}

        {reprog && (
          <div className="card-proxima" style={{ borderLeftColor: "#f59e0b" }}>
            <strong>Reprogramar: {reprog.especialidad}</strong>
            <p className="mini">Elige un nuevo horario:</p>
            <div className="slots">
              {horarios.map((h) => (
                <button key={h.idHorario} className="slot" onClick={() => confirmarReprog(h.idHorario)}>
                  {h.fecha?.slice(5)} {h.horaInicio?.slice(0,5)}
                </button>
              ))}
              {horarios.length === 0 && <span className="mini">Sin horarios disponibles</span>}
            </div>
            <button className="btn-cancelar" onClick={() => setReprog(null)}>Cerrar</button>
          </div>
        )}

        {citas.map((c) => (
          <div className="cita-card" key={c.idCita}>
            <div className="proxima-top">
              <strong>{c.especialidad}</strong>
              <span className={`badge ${c.estado}`}>{c.estado}</span>
            </div>
            <div className="proxima-med">{c.medico}</div>
            <div className="proxima-fecha">{c.establecimiento}</div>
            <div className="proxima-fecha">{c.fecha} · {c.horaInicio?.slice(0,5)}–{c.horaFin?.slice(0,5)}</div>
            <div className="codigo">Código: {c.codigoReserva}</div>
            {c.estado === "CONFIRMADA" && (
              <div className="acciones-cita">
                <button className="btn-mini azul" onClick={() => abrirReprog(c)}>Reprogramar</button>
                <button className="btn-mini rojo" onClick={() => cancelar(c.idCita)}>Cancelar</button>
              </div>
            )}
          </div>
        ))}
        {citas.length === 0 && <div className="card-proxima vacia">Aún no tienes citas. Reserva una desde el inicio.</div>}
      </div>
    </div>
  );
}
