import { useState } from "react";
import { api } from "../api";

export default function Login({ onLogin, irRegistro, irAdmin }) {
  const [dni, setDni] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);

  async function ingresar(e) {
    e.preventDefault();
    setError(null);
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

          <div className="link-derecha">¿Olvidaste tu contraseña?</div>

          {error && <p className="error">⛔ {error}</p>}

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
