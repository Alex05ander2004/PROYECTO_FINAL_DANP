# ReFood — Panel de Administración

Panel web para administradores de ReFood: gestión de productos, pedidos y usuarios.
React + Vite, consumiendo la API REST del backend Django.

## 🚀 En producción

Ya está desplegado en **https://proyecto-final-danp.vercel.app** — pide a un compañero las
credenciales de una cuenta administradora para entrar. Apunta al backend en Render, no hace
falta correr nada en local para probarlo.

## Funcionalidades

| Módulo | Descripción |
|---|---|
| **Login** | Solo para cuentas con rol Administrador (los clientes usan la app Android). |
| **Productos** | Listado, alta, edición y borrado. Precio, descuento, stock, categoría, fecha de vencimiento e imagen. |
| **Pedidos** | Listado de todos los pedidos, con detalle de productos, método de pago (Tarjeta/Yape/Plin) y cambio de estado. |
| **Usuarios** | Listado y gestión de cuentas registradas. |

## Correr en local

### Requisitos

- Node.js 18+
- El backend corriendo (local o apuntando al de Render — ver más abajo)

### Pasos

```bash
npm install
npm run dev
```

Por defecto usa `http://127.0.0.1:8000/api` (backend local). Para apuntar a otro backend
(por ejemplo, el de producción en Render), copia `.env.example` a `.env` y define:

```
VITE_API_BASE_URL=https://proyecto-final-danp.onrender.com/api
```

## Despliegue (Vercel)

- **Root Directory**: `ReFood-Frontend`
- **Framework**: Vite (autodetectado)
- **Variable de entorno**: `VITE_API_BASE_URL` apuntando al backend
- `vercel.json` incluye un rewrite (`/(.*) → /index.html`) necesario para que las rutas de
  React Router (`/login`, `/dashboard`, etc.) no den 404 al navegar directo a ellas.
