import { useEffect, useState } from "react";
import { api } from "../api";

export default function MisCitas() {
  const [citas, setCitas] = useState([]);
  const [error, setError] = useState(null);

  function cargar() {
    api.misCitas().then(setCitas).catch((e) => setError(e.message));
  }
  useEffect(cargar, []);

  async function cancelar(id) {
    if (!confirm("¿Cancelar esta cita?")) return;
    try { await api.cancelar(id); cargar(); }
    catch (e) { setError(e.message); }
  }

  return (
    <div className="screen">
      <div className="topbar">
        <div className="topbar-title">CitaFácil EsSalud</div>
        <div className="topbar-sub">Mis citas</div>
      </div>
      <div className="home-body">
        {error && <p className="error">⛔ {error}</p>}
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
              <button className="btn-cancelar" onClick={() => cancelar(c.idCita)}>Cancelar cita</button>
            )}
          </div>
        ))}
        {citas.length === 0 && <div className="card-proxima vacia">Aún no tienes citas. Reserva una desde el inicio.</div>}
      </div>
    </div>
  );
}
