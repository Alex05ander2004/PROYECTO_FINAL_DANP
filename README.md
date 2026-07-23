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
| [`ReFood-Frontend/`](ReFood-Frontend) | Web de Administradores (React + Vite) |

Cada carpeta tiene su propio `README.md` con instrucciones de instalación y ejecución
específicas de ese proyecto (para desarrollar o correr todo en local).

## 🚀 Probar la app ya desplegada (sin instalar nada)

Backend y panel de administración están en producción — no hace falta levantar nada en tu
PC para probarlos:

| Componente | URL | Notas |
|---|---|---|
| **Panel de Administración** | https://proyecto-final-danp.vercel.app | Pide las credenciales de un admin a un compañero, o crea un cliente registrándote y probando solo la vista de cliente vía la API. |
| **Backend / API** | https://proyecto-final-danp.onrender.com/api/ | Usado tanto por el panel como por la app Android. |
| **App Android** | — | No hay un APK público todavía; clona el repo, abre `ReFood-App/` en Android Studio y ejecuta sobre un emulador o celular. Ya viene configurada para usar el backend de Render por defecto, **no necesitas correr el backend en tu PC**. |

⚠️ El backend está en el plan gratuito de Render: si nadie lo usó en un rato, "se duerme" y
el primer request puede tardar **50 segundos o más** en responder mientras despierta. Es
normal, no es un error — solo espera un poco en el primer login o la primera carga.

### Cuentas de prueba

Pide a un compañero del equipo el email/contraseña de una cuenta administradora existente
para entrar al panel. Para probar como cliente (app Android), puedes registrar una cuenta
nueva directamente desde la pantalla de "Regístrate" — el registro de clientes es abierto.

## Stack e infraestructura en producción

- **Base de datos**: PostgreSQL administrado en [Supabase](https://supabase.com), accedido
  desde Render vía el *connection pooler* (necesario porque Render no soporta salida IPv6 y
  la conexión directa de Supabase resuelve a una IP IPv6).
- **Imágenes de producto**: Supabase Storage (bucket público `product-images`), no en el
  disco del backend (Render tiene disco efímero: cualquier archivo subido localmente se
  perdería en el próximo deploy).
- **Backend**: [Render](https://render.com) (plan free — gunicorn + whitenoise).
- **Panel de administración**: [Vercel](https://vercel.com).
- **Notificaciones push**: Firebase Cloud Messaging, para avisar a los clientes cuando un
  producto que les interesa está por vencer.
