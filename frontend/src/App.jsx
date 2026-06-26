import { useState } from "react";
import Login from "./screens/Login";
import Registro from "./screens/Registro";
import Home from "./screens/Home";
import Reserva from "./screens/Reserva";
import MisCitas from "./screens/MisCitas";
import Perfil from "./screens/Perfil";
import AdminDashboard from "./screens/AdminDashboard";
import BottomNav from "./components/BottomNav";
import "./App.css";

export default function App() {
  const [usuario, setUsuario] = useState(() => {
    const u = localStorage.getItem("usuario");
    return u ? JSON.parse(u) : null;
  });
  // pantallas: login, registro, home, reserva, citas, perfil, admin
  const [screen, setScreen] = useState("login");

  function onLogin(auth) {
    localStorage.setItem("token", auth.token);
    const u = { idAsegurado: auth.idAsegurado, dni: auth.dni, nombres: auth.nombres, apellidos: auth.apellidos };
    localStorage.setItem("usuario", JSON.stringify(u));
    setUsuario(u);
    setScreen("home");
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    setUsuario(null);
    setScreen("login");
  }

  // Dashboard administrativo: vista web a pantalla completa
  if (screen === "admin") {
    return <AdminDashboard onSalir={() => setScreen("login")} />;
  }

  // App del asegurado: marco de telefono
  return (
    <div className="device-wrap">
      <div className="phone">
        {!usuario ? (
          screen === "registro" ? (
            <Registro onLogin={onLogin} irLogin={() => setScreen("login")} />
          ) : (
            <Login onLogin={onLogin} irRegistro={() => setScreen("registro")} irAdmin={() => setScreen("admin")} />
          )
        ) : (
          <>
            <div className="phone-body">
              {screen === "home" && <Home usuario={usuario} ir={setScreen} />}
              {screen === "reserva" && <Reserva volver={() => setScreen("home")} />}
              {screen === "citas" && <MisCitas />}
              {screen === "perfil" && <Perfil usuario={usuario} logout={logout} />}
            </div>
            <BottomNav screen={screen} ir={setScreen} />
          </>
        )}
      </div>
    </div>
  );
}
