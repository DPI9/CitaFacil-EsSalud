import { useEffect, useState } from "react";
import { api } from "../api";

const ICONOS = {
  RECORDATORIO: { color: "#f59e0b", txt: "Recordatorio" },
  CONFIRMACION: { color: "#16a34a", txt: "Confirmación" },
  CANCELACION: { color: "#dc2626", txt: "Cancelación" },
  CUPO_DISPONIBLE: { color: "#0072bc", txt: "Cupo disponible" },
};

export default function Home({ usuario, ir }) {
  const [proxima, setProxima] = useState(null);
  const [notis, setNotis] = useState([]);

  useEffect(() => {
    api.misCitas().then((cs) => {
      const conf = cs.filter((c) => c.estado === "CONFIRMADA")
        .sort((a, b) => (a.fecha + a.horaInicio).localeCompare(b.fecha + b.horaInicio));
      setProxima(conf[0] || null);
    }).catch(() => {});
    api.misNotificaciones().then((n) => setNotis(n.slice(0, 5))).catch(() => {});
  }, []);

  return (
    <div className="screen">
      <div className="topbar">
        <div className="topbar-title">CitaFácil EsSalud</div>
        <div className="topbar-sub">Hola, {usuario.nombres} {usuario.apellidos}</div>
      </div>

      <div className="home-body">
        <h3 className="seccion">Próxima cita</h3>
        {proxima ? (
          <div className="card-proxima">
            <div className="proxima-top">
              <strong>{proxima.especialidad}</strong>
              <span className="badge-verde">Confirmada</span>
            </div>
            <div className="proxima-med">{proxima.medico}</div>
            <div className="proxima-fecha">{proxima.fecha} · {proxima.horaInicio?.slice(0,5)}</div>
          </div>
        ) : (
          <div className="card-proxima vacia">No tienes citas próximas. ¡Reserva una!</div>
        )}

        <h3 className="seccion">Accesos rápidos</h3>
        <div className="accesos">
          <button className="acceso" onClick={() => ir("reserva")}>
            <span className="cuadro" style={{ background: "#0072bc" }} />Reservar cita
          </button>
          <button className="acceso" onClick={() => ir("citas")}>
            <span className="cuadro" style={{ background: "#16a34a" }} />Mis citas
          </button>
          <button className="acceso" onClick={() => ir("reserva")}>
            <span className="cuadro" style={{ background: "#f59e0b" }} />Lista de espera
          </button>
          <button className="acceso" onClick={() => ir("citas")}>
            <span className="cuadro" style={{ background: "#7c3aed" }} />Recordat.
          </button>
        </div>

        <h3 className="seccion">Notificaciones</h3>
        <div className="notis">
          {notis.map((n) => {
            const cfg = ICONOS[n.tipo] || { color: "#9ca3af", txt: n.tipo };
            return (
              <div className="noti" key={n.idNotificacion} style={{ borderLeftColor: cfg.color }}>
                <strong>{cfg.txt}{n.especialidad ? ` · ${n.especialidad}` : ""}</strong>
                <span>{n.fechaEnvio?.replace("T", " ").slice(0, 16)} · {n.canal}</span>
              </div>
            );
          })}
          {notis.length === 0 && <div className="noti vacia">Sin notificaciones</div>}
        </div>
      </div>
    </div>
  );
}
