# PROYECTO_FINAL_DANP — ReFood

Plataforma que conecta clientes y administradores para reducir el desperdicio de alimentos,
permitiendo publicar y adquirir productos próximos a vencer o excedentes de comida a precios
accesibles.

Este repositorio agrupa los tres proyectos que componen la plataforma, cada uno en su propia
carpeta. Todo el trabajo (backend, app y panel) ya está integrado en `main`.

| Carpeta | Proyecto |
|---|---|
| [`ReFood-App/`](ReFood-App/) | App de Clientes (Android, Kotlin, Jetpack Compose) |
| [`ReFood-Backend/`](ReFood-Backend) | Backend (Django + Django REST Framework, PostgreSQL) |
| [`ReFood-Frontend/`](ReFood-Frontend) | Web de Administradores (React + Vite, Tailwind) |

## 🚀 Probar la app desplegada

Paneles de administración, tanto backend, como frontend ya se encuentran en producción:

| Componente | URL | Notas |
|---|---|---|
| **Panel de Administración** | https://proyecto-final-danp.vercel.app | Panel de administrador en frontend (oficial). |
| **Backend (admin)** | https://proyecto-final-danp.onrender.com/admin/ | Panel de administrador en backend. |
| **API** | https://proyecto-final-danp.onrender.com/api/ | Usado tanto por el panel frontend, como por la app Android. |
| **App Android** | — | No hay un APK público todavía; se debe abrir `ReFood-App/` en Android Studio y ejecutar sobre un emulador o celular. |

⚠️ El backend está en el plan gratuito de Render, por lo cual, si no recibe solicitudes pasado un tiempo, el servidor "se duerme" y el primer request puede tardar **50 segundos o más** en responder mientras el servidor "despierta".

### Cuentas de prueba

Solicitar correo y contraseña de administrador a uno de los desarrolladores del proyecto.

## Stack e infraestructura en producción

- **Base de datos**: PostgreSQL administrado en [Supabase](https://supabase.com), accedido desde Render vía el *connection pooler* (necesario porque Render no soporta salida IPv6 y la conexión directa de Supabase resuelve a una IP IPv6).
- **Imágenes de producto**: Supabase Storage (bucket público `product-images`), no en el disco del backend (Render tiene disco efímero: cualquier archivo subido localmente se perdería en el próximo deploy).
- **Backend**: [Render](https://render.com) (plan free — gunicorn + whitenoise).
- **Panel de administración**: [Vercel](https://vercel.com).
- **Notificaciones push**: Firebase Cloud Messaging, para avisar a los clientes cuando un producto está por vencer.
