// Ruta relativa: en desarrollo la resuelve el proxy de Vite y en produccion
// el reverse proxy de Nginx. Asi funciona igual en local, Docker y en la nube.
const BASE = "/api";

function getToken() {
  return localStorage.getItem("token");
}

async function request(path, { method = "GET", body, auth = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth && getToken()) headers["Authorization"] = `Bearer ${getToken()}`;

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error(data?.mensaje || `Error ${res.status}`);
  }
  return data;
}

export const api = {
  // Auth
  registro: (datos) => request("/auth/registro", { method: "POST", body: datos }),
  login: (datos) => request("/auth/login", { method: "POST", body: datos }),
  // Catalogo
  especialidades: () => request("/especialidades"),
  medicos: (especialidad) =>
    request(`/medicos${especialidad ? `?especialidad=${encodeURIComponent(especialidad)}` : ""}`),
  horarios: (especialidad) =>
    request(`/horarios${especialidad ? `?especialidad=${encodeURIComponent(especialidad)}` : ""}`),
  horariosMedico: (idMedico) => request(`/horarios/medico/${idMedico}`),
  // Citas (requieren token)
  reservar: (idHorario) => request("/citas", { method: "POST", auth: true, body: { idHorario, canal: "WEB" } }),
  misCitas: () => request("/citas/mias", { auth: true }),
  cancelar: (idCita) => request(`/citas/${idCita}`, { method: "DELETE", auth: true }),
  reprogramar: (idCita, idHorario) => request(`/citas/${idCita}/reprogramar`, { method: "PUT", auth: true, body: { idHorario } }),
  listaEspera: (idHorario) => request(`/citas/lista-espera/${idHorario}`, { method: "POST", auth: true }),
  recuperar: (dni) => request("/auth/recuperar", { method: "POST", body: { dni } }),
  // Notificaciones
  misNotificaciones: () => request("/notificaciones/mias", { auth: true }),
  // Admin
  kpis: () => request("/admin/kpis"),
  adminCitas: (estado, hoy) => request(`/admin/citas?${estado ? `estado=${estado}&` : ""}${hoy ? "hoy=true" : ""}`),
  adminListaEspera: () => request("/admin/lista-espera"),
  adminReportes: () => request("/admin/reportes"),
  adminAsegurados: () => request("/admin/asegurados"),
  adminReservar: (dni, idHorario) => request("/admin/reservar", { method: "POST", body: { dni, idHorario } }),
  adminAuditoria: () => request("/admin/auditoria"),
  adminMedicos: () => request("/admin/medicos"),
  adminAgenda: (idMedico) => request(`/admin/agenda/${idMedico}`),
  adminEjecutarRecordatorios: () => request("/admin/recordatorios/ejecutar", { method: "POST" }),
};

export { getToken };
