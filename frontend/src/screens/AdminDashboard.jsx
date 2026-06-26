import { useEffect, useState } from "react";
import { api } from "../api";

const COLOR_ESTADO = { CONFIRMADA: "#16a34a", ATENDIDA: "#0072bc", NO_ASISTIO: "#dc2626", CANCELADA: "#9ca3af" };

const MENU = [
  { key: "dashboard", label: "Dashboard" },
  { key: "nueva", label: "➕ Nueva reserva" },
  { key: "citasdia", label: "Citas del día" },
  { key: "reservas", label: "Reservas" },
  { key: "cancelaciones", label: "Cancelaciones" },
  { key: "espera", label: "Lista de espera" },
  { key: "reportes", label: "Reportes" },
  { key: "config", label: "Configuración" },
];

const TITULOS = {
  dashboard: "Dashboard", nueva: "Nueva reserva", citasdia: "Citas del día", reservas: "Reservas",
  cancelaciones: "Cancelaciones", espera: "Lista de espera", reportes: "Reportes", config: "Configuración",
};

export default function AdminDashboard({ onSalir }) {
  const [seccion, setSeccion] = useState("dashboard");
  const hoy = new Date().toLocaleDateString("es-PE", { day: "numeric", month: "long", year: "numeric" });

  return (
    <div className="admin">
      <aside className="admin-side">
        <div className="admin-logo">CitaFácil<span>Panel administrativo</span></div>
        <nav>
          {MENU.map((m) => (
            <a key={m.key} className={seccion === m.key ? "activo" : ""} onClick={() => setSeccion(m.key)}>
              {m.label}
            </a>
          ))}
        </nav>
        <button className="admin-salir" onClick={onSalir}>← Salir</button>
      </aside>

      <main className="admin-main">
        <header className="admin-top">
          <div><h1>{TITULOS[seccion]}</h1><span>{hoy}</span></div>
          <div className="admin-user">Admin EsSalud <span className="admin-bubble" /></div>
        </header>

        {seccion === "dashboard" && <Dashboard />}
        {seccion === "nueva" && <NuevaReserva />}
        {seccion === "citasdia" && <TablaCitas titulo="Citas programadas para hoy" hoy />}
        {seccion === "reservas" && <TablaCitas titulo="Todas las reservas" />}
        {seccion === "cancelaciones" && <TablaCitas titulo="Citas canceladas" estado="CANCELADA" />}
        {seccion === "espera" && <ListaEspera />}
        {seccion === "reportes" && <Reportes />}
        {seccion === "config" && <Config />}
      </main>
    </div>
  );
}

/* ---------- Dashboard ---------- */
function Dashboard() {
  const [k, setK] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => { api.kpis().then(setK).catch((e) => setError(e.message)); }, []);
  const maxOcup = k ? Math.max(1, ...k.ocupacionPorHora.map((o) => o.cantidad)) : 1;

  if (error) return <p className="error">⛔ {error} (¿backend encendido?)</p>;
  if (!k) return <p>Cargando indicadores…</p>;

  return (
    <>
      <section className="kpis">
        <Kpi color="#0072bc" titulo="Citas hoy" valor={k.citasHoy} />
        <Kpi color="#dc2626" titulo="Cancelaciones" valor={k.cancelacionesHoy} />
        <Kpi color="#f59e0b" titulo="En lista de espera" valor={k.enListaEspera} />
        <Kpi color="#16a34a" titulo="Tasa de asistencia" valor={`${k.tasaAsistencia}%`} />
      </section>
      <section className="admin-grid">
        <div className="panel">
          <h3>Ocupación por hora</h3>
          <div className="chart">
            {k.ocupacionPorHora.map((o) => (
              <div className="bar-col" key={o.hora}>
                <div className="bar" style={{ height: `${(o.cantidad / maxOcup) * 100}%` }} title={`${o.cantidad} citas`} />
                <span>{o.hora.slice(0, 2)}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="panel">
          <h3>Próximas citas</h3>
          <ul className="prox-list">
            {k.proximasCitas.map((c, i) => (
              <li key={i}>
                <span className="prox-hora">{c.hora?.slice(0, 5)}</span>
                <span className="prox-pac">{c.paciente}</span>
                <span className="prox-esp">{c.especialidad}</span>
                <span className="prox-dot" style={{ background: COLOR_ESTADO[c.estado] || "#9ca3af" }} />
              </li>
            ))}
            {k.proximasCitas.length === 0 && <li className="vacio">Sin próximas citas</li>}
          </ul>
        </div>
      </section>
      <section className="panel">
        <h3>Alertas operativas</h3>
        <div className="alertas">
          {k.alertas.map((a, i) => (
            <div className={`alerta ${a.nivel}`} key={i}><strong>{a.titulo}</strong><span>{a.detalle}</span></div>
          ))}
          {k.alertas.length === 0 && <div className="alerta ok"><strong>Todo en orden</strong><span>No hay alertas operativas</span></div>}
        </div>
      </section>
    </>
  );
}

/* ---------- Tabla de citas reutilizable ---------- */
function TablaCitas({ titulo, estado, hoy }) {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => {
    api.adminCitas(estado, hoy).then(setRows).catch((e) => setError(e.message));
  }, [estado, hoy]);

  return (
    <section className="panel">
      <h3>{titulo}</h3>
      {error && <p className="error">⛔ {error}</p>}
      {!rows ? <p>Cargando…</p> : (
        <table className="tabla">
          <thead><tr><th>Código</th><th>Paciente</th><th>DNI</th><th>Especialidad</th><th>Médico</th><th>Fecha</th><th>Hora</th><th>Estado</th></tr></thead>
          <tbody>
            {rows.map((c, i) => (
              <tr key={i}>
                <td>{c.codigo}</td><td>{c.paciente}</td><td>{c.dni}</td>
                <td>{c.especialidad}</td><td>{c.medico}</td>
                <td>{c.fecha}</td><td>{c.hora?.slice(0, 5)}</td>
                <td><span className="pill" style={{ background: COLOR_ESTADO[c.estado] || "#9ca3af" }}>{c.estado}</span></td>
              </tr>
            ))}
            {rows.length === 0 && <tr><td colSpan={8} className="vacio">Sin registros</td></tr>}
          </tbody>
        </table>
      )}
    </section>
  );
}

/* ---------- Lista de espera ---------- */
function ListaEspera() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => { api.adminListaEspera().then(setRows).catch((e) => setError(e.message)); }, []);
  return (
    <section className="panel">
      <h3>Lista de espera</h3>
      {error && <p className="error">⛔ {error}</p>}
      {!rows ? <p>Cargando…</p> : (
        <table className="tabla">
          <thead><tr><th>#</th><th>Paciente</th><th>DNI</th><th>Especialidad</th><th>Médico</th><th>Fecha</th><th>Hora</th><th>Estado</th></tr></thead>
          <tbody>
            {rows.map((e, i) => (
              <tr key={i}><td>{e.posicion}</td><td>{e.paciente}</td><td>{e.dni}</td><td>{e.especialidad}</td><td>{e.medico}</td><td>{e.fecha}</td><td>{e.hora?.slice(0,5)}</td><td>{e.estado}</td></tr>
            ))}
            {rows.length === 0 && <tr><td colSpan={8} className="vacio">No hay nadie en lista de espera</td></tr>}
          </tbody>
        </table>
      )}
    </section>
  );
}

/* ---------- Reportes ---------- */
function Reportes() {
  const [r, setR] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => { api.adminReportes().then(setR).catch((e) => setError(e.message)); }, []);
  if (error) return <p className="error">⛔ {error}</p>;
  if (!r) return <p>Cargando…</p>;
  return (
    <>
      <section className="kpis">
        <Kpi color="#0072bc" titulo="Total de citas" valor={r.totalCitas} />
        <Kpi color="#16a34a" titulo="Atendidas" valor={r.porEstado.ATENDIDA} />
        <Kpi color="#dc2626" titulo="Canceladas" valor={r.porEstado.CANCELADA} />
        <Kpi color="#f59e0b" titulo="No asistió" valor={r.porEstado.NO_ASISTIO} />
      </section>
      <section className="admin-grid">
        <div className="panel">
          <h3>Citas por estado</h3>
          <table className="tabla"><tbody>
            {Object.entries(r.porEstado).map(([e, v]) => (<tr key={e}><td>{e}</td><td><strong>{v}</strong></td></tr>))}
          </tbody></table>
        </div>
        <div className="panel">
          <h3>Citas por especialidad</h3>
          <table className="tabla"><tbody>
            {Object.entries(r.porEspecialidad).map(([e, v]) => (<tr key={e}><td>{e}</td><td><strong>{v}</strong></td></tr>))}
          </tbody></table>
        </div>
      </section>
    </>
  );
}

/* ---------- Nueva reserva (módulo recepcionista) ---------- */
function NuevaReserva() {
  const [asegurados, setAsegurados] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const [dni, setDni] = useState("");
  const [especialidad, setEspecialidad] = useState("");
  const [medicos, setMedicos] = useState([]);
  const [idMedico, setIdMedico] = useState("");
  const [horarios, setHorarios] = useState([]);
  const [idHorario, setIdHorario] = useState("");
  const [msg, setMsg] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.adminAsegurados().then(setAsegurados).catch(() => {});
    api.especialidades().then(setEspecialidades).catch(() => {});
  }, []);

  function cambioEspecialidad(e) {
    const esp = e.target.value;
    setEspecialidad(esp); setIdMedico(""); setHorarios([]); setIdHorario("");
    if (esp) api.medicos(esp).then(setMedicos).catch(() => setMedicos([]));
    else setMedicos([]);
  }

  function cambioMedico(e) {
    const id = e.target.value;
    setIdMedico(id); setIdHorario("");
    if (id) api.horariosMedico(id).then((hs) => setHorarios(hs.filter((h) => h.cuposDisponibles > 0)))
      .catch(() => setHorarios([]));
    else setHorarios([]);
  }

  async function agendar(e) {
    e.preventDefault();
    setMsg(null); setError(null);
    try {
      const cita = await api.adminReservar(dni, Number(idHorario));
      setMsg(`✅ Cita agendada para DNI ${dni}. Código: ${cita.codigoReserva} (${cita.especialidad}, ${cita.fecha} ${cita.horaInicio?.slice(0,5)})`);
      // refrescar horarios para reflejar el cupo descontado
      if (idMedico) api.horariosMedico(idMedico).then((hs) => setHorarios(hs.filter((h) => h.cuposDisponibles > 0)));
      setIdHorario("");
    } catch (err) {
      setError(err.message);
    }
  }

  const puedeAgendar = dni && idHorario;

  return (
    <section className="panel" style={{ maxWidth: 640 }}>
      <h3>Agendar cita a nombre de un paciente</h3>
      {msg && <p className="ok-admin">{msg}</p>}
      {error && <p className="error">⛔ {error}</p>}

      <form onSubmit={agendar} className="form-admin">
        <label>1. Paciente (DNI)</label>
        <select value={dni} onChange={(e) => setDni(e.target.value)} required>
          <option value="">-- Selecciona un paciente --</option>
          {asegurados.map((a) => (
            <option key={a.idAsegurado} value={a.dni}>{a.dni} — {a.nombre}</option>
          ))}
        </select>

        <label>2. Especialidad</label>
        <select value={especialidad} onChange={cambioEspecialidad} required>
          <option value="">-- Selecciona --</option>
          {especialidades.map((e) => <option key={e} value={e}>{e}</option>)}
        </select>

        <label>3. Médico</label>
        <select value={idMedico} onChange={cambioMedico} disabled={!especialidad} required>
          <option value="">-- Selecciona --</option>
          {medicos.map((m) => <option key={m.idMedico} value={m.idMedico}>Dr(a). {m.nombres} {m.apellidos}</option>)}
        </select>

        <label>4. Horario disponible</label>
        <select value={idHorario} onChange={(e) => setIdHorario(e.target.value)} disabled={!idMedico} required>
          <option value="">-- Selecciona --</option>
          {horarios.map((h) => (
            <option key={h.idHorario} value={h.idHorario}>
              {h.fecha} · {h.horaInicio?.slice(0,5)}–{h.horaFin?.slice(0,5)} ({h.cuposDisponibles} cupos)
            </option>
          ))}
        </select>

        <button className="btn-admin" disabled={!puedeAgendar}>Agendar cita</button>
      </form>
    </section>
  );
}

/* ---------- Configuración ---------- */
function Config() {
  return (
    <section className="panel">
      <h3>Configuración del sistema</h3>
      <table className="tabla"><tbody>
        <tr><td>Sistema</td><td><strong>CitaFácil EsSalud</strong></td></tr>
        <tr><td>Versión</td><td>1.0 (MVP)</td></tr>
        <tr><td>Backend</td><td>Java 21 + Spring Boot 3.5</td></tr>
        <tr><td>Base de datos</td><td>PostgreSQL 17</td></tr>
        <tr><td>Frontend</td><td>React 18 + Vite</td></tr>
        <tr><td>Seguridad</td><td>JWT + BCrypt (factor 12)</td></tr>
        <tr><td>Notificaciones</td><td>WhatsApp / SMS (simulado)</td></tr>
      </tbody></table>
    </section>
  );
}

function Kpi({ color, titulo, valor }) {
  return (
    <div className="kpi" style={{ borderTopColor: color }}>
      <span className="kpi-titulo">{titulo}</span>
      <span className="kpi-valor">{valor}</span>
    </div>
  );
}
