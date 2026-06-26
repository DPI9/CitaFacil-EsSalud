export default function Perfil({ usuario, logout }) {
  return (
    <div className="screen">
      <div className="topbar">
        <div className="topbar-title">CitaFácil EsSalud</div>
        <div className="topbar-sub">Mi perfil</div>
      </div>
      <div className="home-body">
        <div className="perfil-avatar">{usuario.nombres?.[0]}{usuario.apellidos?.[0]}</div>
        <div className="card-proxima">
          <div className="proxima-top"><strong>{usuario.nombres} {usuario.apellidos}</strong></div>
          <div className="proxima-fecha">DNI: {usuario.dni}</div>
        </div>
        <button className="btn-cancelar" onClick={logout}>Cerrar sesión</button>
      </div>
    </div>
  );
}
