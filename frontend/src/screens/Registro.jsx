import { useState } from "react";
import { api } from "../api";

export default function Registro({ onLogin, irLogin }) {
  const [form, setForm] = useState({ dni: "", nombres: "", apellidos: "", telefono: "", correo: "", contrasena: "" });
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function crear(e) {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      const auth = await api.registro(form);
      onLogin(auth);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="screen">
      <div className="topbar">
        <button className="topbar-back" onClick={irLogin}>‹</button>
        <div>
          <div className="topbar-title">CitaFácil EsSalud</div>
          <div className="topbar-sub">Crear cuenta nueva</div>
        </div>
      </div>

      <div className="login-body scroll">
        <form onSubmit={crear} className="form">
          <label>DNI</label>
          <input value={form.dni} onChange={set("dni")} maxLength={8} placeholder="12345678" required />
          <label>Nombres</label>
          <input value={form.nombres} onChange={set("nombres")} required />
          <label>Apellidos</label>
          <input value={form.apellidos} onChange={set("apellidos")} required />
          <label>Teléfono</label>
          <input value={form.telefono} onChange={set("telefono")} placeholder="999888777" />
          <label>Correo</label>
          <input type="email" value={form.correo} onChange={set("correo")} />
          <label>Contraseña</label>
          <input type="password" value={form.contrasena} onChange={set("contrasena")} placeholder="mínimo 6 caracteres" required />

          {error && <p className="error">⛔ {error}</p>}

          <button className="btn-primario" disabled={cargando}>
            {cargando ? "CREANDO…" : "CREAR CUENTA"}
          </button>
        </form>
        <button className="btn-outline" onClick={irLogin}>Ya tengo cuenta</button>
      </div>
    </div>
  );
}
