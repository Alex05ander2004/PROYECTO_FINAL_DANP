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

- Panel de administración: `http://127.0.0.1:8000/admin/`
- Vista pública de productos: `http://127.0.0.1:8000/`
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

## Endpoints principales

| Función | Endpoint | Método |
|---|---|---|
| Registro (app) | `/api/auth/register/` | POST |
| Login (app) | `/api/auth/login/` | POST |
| Refrescar token | `/api/auth/refresh/` | POST |
| Perfil propio | `/api/auth/me/` | GET/PATCH |
| Listar productos | `/api/products/` | GET |
| Listar categorías | `/api/products/categories/` | GET |
| Ver carrito | `/api/orders/cart/` | GET |
| Agregar al carrito | `/api/orders/cart/` | POST |
| Confirmar pedido | `/api/orders/checkout/` | POST |
| Mis pedidos | `/api/orders/` | GET |

Todos los endpoints (excepto registro/login) requieren el header:
```
Authorization: Bearer <access_token>
```

## Notas

- El pago es simulado; no hay integración con pasarelas reales.
- Los administradores no se autorregistran: se crean vía `createsuperuser` o desde el panel `/admin/` por otro administrador.
