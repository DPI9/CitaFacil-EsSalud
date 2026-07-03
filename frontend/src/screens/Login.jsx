import { useState } from "react";
import { api } from "../api";

export default function Login({ onLogin, irRegistro, irAdmin }) {
  const [dni, setDni] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [error, setError] = useState(null);
  const [msg, setMsg] = useState(null);
  const [cargando, setCargando] = useState(false);

  async function ingresar(e) {
    e.preventDefault();
    setError(null); setMsg(null);
    setCargando(true);
    try {
      const auth = await api.login({ dni, contrasena });
      onLogin(auth);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function recuperar() {
    setError(null); setMsg(null);
    if (!dni) { setError("Ingresa tu DNI primero para recuperar la contraseña."); return; }
    try {
      const r = await api.recuperar(dni);
      setMsg(`📧 ${r.mensaje} (Demo — contraseña temporal: ${r.contrasenaTemporal})`);
    } catch (err) { setError(err.message); }
  }

  return (
    <div className="screen login">
      <div className="topbar">
        <div className="topbar-title">CitaFácil EsSalud</div>
        <div className="topbar-sub">Iniciar sesión</div>
      </div>

      <div className="login-body">
        <div className="logo-circle">+</div>
        <h2 className="login-h">Bienvenido al sistema</h2>
        <p className="login-p">Ingresa con tu DNI y contraseña</p>

        <form onSubmit={ingresar} className="form">
          <label>DNI</label>
          <input value={dni} onChange={(e) => setDni(e.target.value)} maxLength={8} placeholder="Ingresa tu DNI" required />

          <label>Contraseña</label>
          <input type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} placeholder="••••••••" required />

          <div className="link-derecha" onClick={recuperar}>¿Olvidaste tu contraseña?</div>

          {error && <p className="error">⛔ {error}</p>}
          {msg && <p className="ok">{msg}</p>}

          <button className="btn-primario" disabled={cargando}>
            {cargando ? "INGRESANDO…" : "INGRESAR"}
          </button>
        </form>

        <div className="divisor"><span>o</span></div>

        <button className="btn-outline" onClick={irRegistro}>CREAR CUENTA NUEVA</button>

        <button className="link-admin" onClick={irAdmin}>Panel administrativo →</button>
      </div>
    </div>
  );
}
