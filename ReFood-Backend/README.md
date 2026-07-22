# ReFood — Backend

Backend en Django + Django REST Framework para la plataforma ReFood.

## Requisitos previos

- Python 3.10 o superior
- PostgreSQL (con pgAdmin recomendado)
- Git

## 1. Crear y activar el entorno virtual

**Windows (Git Bash):**
```bash
python -m venv venv
source venv/Scripts/activate
```

**Linux/Mac:**
```bash
python3 -m venv venv
source venv/bin/activate
```

## 2. Instalar dependencias

```bash
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

## 3. Configurar PostgreSQL

Usando pgAdmin:

1. Crea una base de datos, ej. `refood_db`
2. Crea un rol/usuario, ej. `refood_user`, con una contraseña, con **Can login = Yes**
3. Dale privilegios sobre la base de datos y el esquema `public` (necesario desde PostgreSQL 15+):

```sql
GRANT ALL PRIVILEGES ON DATABASE refood_db TO refood_user;
GRANT ALL ON SCHEMA public TO refood_user;
ALTER SCHEMA public OWNER TO refood_user;
```

## 4. Configurar variables de entorno

Copia la plantilla y completa tus propios valores:

```bash
cp .env.example .env
```

Genera tu propio `SECRET_KEY`:

```bash
python -c "from django.core.management.utils import get_random_secret_key; print(get_random_secret_key())"
```

Pega el resultado en `.env`, junto con los datos de tu base de datos local.

## 5. Aplicar migraciones

```bash
python manage.py migrate
```

## 6. Crear un superusuario (administrador)

```bash
python manage.py createsuperuser
```

Te pedirá **email**, **name** y **password**.

## 7. Levantar el servidor

```bash
python manage.py runserver
```

- Panel de administración de Django: `http://127.0.0.1:8000/admin/`
- API: `http://127.0.0.1:8000/api/`

### Probar desde un celular físico (app Android)

`127.0.0.1`/`localhost` solo funciona si el cliente corre en la misma PC. Para
probar desde un celular conectado a la misma WiFi:

1. Agrega tu IP de red local a `ALLOWED_HOSTS` en tu `.env` (ver `.env.example`).
   Consíguela con `ipconfig` (Windows) o `ifconfig`/`ip addr` (Linux/Mac).
2. Levanta el servidor escuchando en todas las interfaces, no solo localhost:
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```
3. En la app, usa `http://<tu-ip-de-red>:8000/` como URL base.

Si usas el emulador de Android Studio (no un celular físico), no hace falta
nada de esto: el alias `10.0.2.2` ya apunta al `localhost` de tu PC.

## Notificaciones de vencimiento (Firebase)

Cuando a un producto le queda poco tiempo, un comando sube su descuento
automáticamente y envía una notificación push a los usuarios registrados:

| Días restantes | Descuento mínimo |
|---|---|
| ≤ 30 | 20% |
| ≤ 7 | 40% |
| ≤ 2 | 60% |

Requiere el archivo de credenciales de Firebase Admin (`secrets/firebase-adminsdk.json`,
**nunca se sube a git**) y `FIREBASE_CREDENTIALS_PATH` en tu `.env` si lo guardaste
en otra ruta.

Para probarlo manualmente:
```bash
python manage.py check_expiring_products
```

Como el proyecto no usa Celery, este comando debe programarse para correr una
vez al día **en cada máquina donde corra el backend** (el Programador de
tareas/cron es local al sistema operativo, no se sincroniza vía git):

- **Windows** (PowerShell, como administrador si hace falta):
  ```powershell
  $action = New-ScheduledTaskAction -Execute "C:\ruta\a\ReFood-Backend\venv\Scripts\python.exe" -Argument "manage.py check_expiring_products" -WorkingDirectory "C:\ruta\a\ReFood-Backend"
  $trigger = New-ScheduledTaskTrigger -Daily -At 8:00AM
  Register-ScheduledTask -TaskName "ReFood_CheckExpiringProducts" -Action $action -Trigger $trigger -Description "Sube descuentos y envia notificaciones push para productos proximos a vencer."
  ```
  (o lo mismo desde la interfaz: Programador de tareas → crear tarea básica →
  acción "Iniciar un programa" → apuntar al `python.exe` del `venv` con el
  argumento `manage.py check_expiring_products` y "Iniciar en" la carpeta del
  proyecto.)
- **Linux/Mac**: agregar una línea a `crontab -e`, ej. todos los días a las 8am:
  ```
  0 8 * * * cd /ruta/a/ReFood-Backend && venv/bin/python manage.py check_expiring_products
  ```

## Endpoints principales

| Función | Endpoint | Método |
|---|---|---|
| Registro (app) | `/api/auth/register/` | POST |
| Login (app) | `/api/auth/login/` | POST |
| Refrescar token | `/api/auth/refresh/` | POST |
| Perfil propio | `/api/auth/me/` | GET/PATCH |
| Listar/editar usuarios (solo admin) | `/api/auth/users/` | GET, `/api/auth/users/{id}/` PATCH |
| Listar/crear productos (crear/editar/borrar solo admin) | `/api/products/` | GET/POST, `/api/products/{id}/` PATCH/DELETE |
| Listar categorías | `/api/products/categories/` | GET |
| Ver/agregar al carrito | `/api/orders/cart/` | GET/POST, `/api/orders/cart/{id}/` PATCH/DELETE |
| Confirmar pedido | `/api/orders/checkout/` | POST |
| Mis pedidos (admin ve todos, cambia estado) | `/api/orders/` | GET, `/api/orders/{id}/` PATCH (solo admin) |

Todos los endpoints (excepto registro/login) requieren el header:
```
Authorization: Bearer <access_token>
```

`login/`, `register/` y el resto de la API tienen límite de peticiones
(throttling) para evitar fuerza bruta — ver `DEFAULT_THROTTLE_RATES` en
`config/settings.py`.

## Tests

```bash
python manage.py test
```

Corre contra una base SQLite en memoria (no toca la base de Postgres real ni
requiere permisos extra). Cubre autenticación, permisos por rol (cliente vs.
administrador), carrito/checkout (incluida validación de stock) y el comando
`check_expiring_products`.

## Notas

- El pago es simulado; no hay integración con pasarelas reales.
- Los administradores no se autorregistran: se crean vía `createsuperuser` o desde el panel `/admin/` por otro administrador.
