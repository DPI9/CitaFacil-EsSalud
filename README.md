# CitaFácil EsSalud

Plataforma digital para agilizar y priorizar citas médicas en Lima.
Proyecto del curso **Innovación y Transformación Digital** — UTP, Sección 32017.

## Stack tecnológico
- **Backend:** Java 21 + Spring Boot 3.5 (Web, Data JPA, Security, Validation, Lombok)
- **Base de datos:** PostgreSQL 17
- **Frontend:** React 18 + Vite
- **Seguridad:** BCrypt (factor 12), preparado para JWT

## Estructura
```
CitaFacil-EsSalud/
├── backend/      → API REST en Spring Boot
├── frontend/     → Aplicación React (Vite)
└── database/     → schema.sql (modelo físico)
```

## Requisitos instalados
- JDK 21+ · Maven · Node.js 18+ · PostgreSQL 17 · Git

## Puesta en marcha

### 1. Base de datos
```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE citafacil;"
```
Las tablas se crean solas al arrancar el backend (Hibernate `ddl-auto=update`).
El esquema documentado está en `database/schema.sql`.

### 2. Backend (puerto 8080)
```powershell
cd backend
# Indica la contraseña de tu usuario postgres:
$env:DB_PASSWORD = "TU_PASSWORD"
.\mvnw.cmd spring-boot:run
```
Prueba: http://localhost:8080/api/health

### 3. Frontend (puerto 5173)
```powershell
cd frontend
npm install
npm run dev
```
Abre: http://localhost:5173

## Endpoints iniciales
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/health` | Estado del sistema |
| GET | `/api/establecimientos` | Lista de establecimientos |
| POST | `/api/establecimientos` | Crear establecimiento |
| GET | `/api/medicos?especialidad=` | Lista de médicos (filtro opcional) |

> Esta es la **versión inicial (Sprint 1)**: arranque del sistema, modelo de datos
> y conexión backend–frontend–BD. Las funciones de reserva, lista de espera y
> notificaciones se implementan en los siguientes sprints.
