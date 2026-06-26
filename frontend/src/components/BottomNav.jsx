const ITEMS = [
  { key: "home", label: "Inicio", icon: "🏠" },
  { key: "citas", label: "Citas", icon: "📅" },
  { key: "reserva", label: "Reservar", icon: "➕" },
  { key: "perfil", label: "Perfil", icon: "👤" },
];

export default function BottomNav({ screen, ir }) {
  return (
    <nav className="bottom-nav">
      {ITEMS.map((it) => (
        <button key={it.key} className={screen === it.key ? "nav-item activo" : "nav-item"} onClick={() => ir(it.key)}>
          <span className="nav-icon">{it.icon}</span>
          <span className="nav-label">{it.label}</span>
        </button>
      ))}
    </nav>
  );
}
