# 🥗 ReFood — App de Clientes

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white)
![Architecture](https://img.shields.io/badge/Arquitectura-MVVM-orange)

**ReFood** es una solución tecnológica que conecta **clientes** y **administradores** para reducir el desperdicio de alimentos, permitiendo publicar y adquirir productos próximos a vencer o excedentes de comida a precios accesibles.

Esta carpeta contiene la **aplicación Android de Clientes**, desarrollada en Kotlin con Jetpack Compose y arquitectura MVVM, consumiendo la API REST del backend Django compartida con el panel de administración.

---

## 📱 Funcionalidades

| Módulo | Descripción |
|---|---|
| **Autenticación** | Registro e inicio de sesión contra el backend, con validación real de formularios y sesión persistente (JWT, con refresh automático). |
| **Pantalla principal** | Saludo personalizado, ofertas especiales destacadas y productos disponibles. |
| **Productos** | Catálogo completo con búsqueda por nombre y filtro por categoría. |
| **Detalle de producto** | Descripción, precio con descuento, stock, fecha de vencimiento (con indicador de urgencia) y selector de cantidad. |
| **Ofertas especiales** | Productos próximos a vencer con descuento destacado. |
| **Carrito de compra** | Edición de cantidades (respetando el stock real), eliminación de ítems y cálculo de total en tiempo real. |
| **Formulario de pedido** | Dirección de entrega, método de pago (**Tarjeta**, Yape o Plin, con validaciones reales de cada uno) y notas para el negocio. |
| **Confirmación de pedido** | Resumen de éxito con número de pedido. |
| **Mis pedidos** | Historial de pedidos con estado (pendiente, en preparación, listo, entregado, cancelado). |
| **Detalle de pedido** | Resumen completo de productos, total, entrega y pago. |
| **Perfil** | Edición de datos personales y cierre de sesión. |
| **Notificaciones push** | Aviso vía Firebase Cloud Messaging cuando un producto está por vencer. |

Sobre el pago con **Tarjeta**: es simulado (no hay pasarela real), pero pide número,
vencimiento, CVV y titular como en un formulario real. Por seguridad, el número completo y
el CVV nunca se envían al backend ni se guardan en ningún lado — solo se envían los
**últimos 4 dígitos** (igual que cualquier confirmación de pago real), reutilizando el mismo
campo que ya existía para el número de operación de Yape/Plin.

---

## 🏗️ Arquitectura

MVVM organizado por capas:

```
ui/            Composables (pantallas + componentes reutilizables) y ViewModels
domain/        Modelos de dominio y validadores de formularios
data/
  remote/      Retrofit: APIs, DTOs y NetworkModule (auth con refresh automático de token)
  repository/  Implementaciones remotas (Remote*RepositoryImpl) sobre esas APIs
  session/     Sesión de usuario (DataStore Preferences)
di/            Inyección de dependencias manual (AppContainer + ViewModelFactory)
navigation/    Rutas y grafo de navegación (Navigation Compose)
notifications/ Servicio de Firebase Cloud Messaging
```

**Persistencia**: no hay base de datos local — todos los datos (productos, carrito,
pedidos, usuarios) viven en el backend compartido con el panel de administración, consumido
vía Retrofit/OkHttp.

---

## 🛠️ Stack tecnológico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** con `ViewModel`, `StateFlow` y `Coroutines`
- **Navigation Compose** para la navegación entre pantallas
- **Retrofit + OkHttp** para consumir la API REST (JWT con refresh automático)
- **DataStore Preferences** para la sesión del usuario
- **Coil** para carga de imágenes remotas
- **Firebase Cloud Messaging** para notificaciones push
- Inyección de dependencias manual (`AppContainer`), sin frameworks externos

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos

- **Android Studio** (Ladybug o superior recomendado)
- **JDK 11+** (se recomienda usar el JDK embebido de Android Studio)
- Emulador o dispositivo físico con **Android 7.0 (API 24)** o superior
- Conexión a internet (consume la API real del backend)

### Pasos

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Alex05ander2004/PROYECTO_FINAL_DANP.git
   ```
2. Abre la carpeta `ReFood-App/` en **Android Studio**.
3. Deja que Gradle sincronice las dependencias.
4. Ejecuta la app (▶) sobre un emulador o dispositivo conectado.

Por defecto la app apunta al **backend en producción** (Render), así que funciona sin
levantar nada más — solo necesitas conexión a internet. Ten en cuenta que el backend está en
el plan gratuito de Render: si nadie lo usó en un rato, el primer request puede tardar
**50 segundos o más** mientras "despierta" (la app espera hasta 60s antes de mostrar error).

### Apuntar a un backend local (opcional, para desarrollar)

Si estás desarrollando el backend y quieres probar contra tu propia PC en vez de Render,
crea (o edita) `local.properties` en la raíz del proyecto y agrega:

```properties
REFOOD_API_BASE_URL=http://10.0.2.2:8000/
```

(`10.0.2.2` es el alias que usa el emulador de Android Studio para apuntar al `localhost` de
tu PC; para un celular físico en la misma WiFi, usa la IP de red local de tu PC en su lugar).
`local.properties` no se sube a git — es configuración de cada desarrollador.

---

## 📂 Estructura del proyecto

```
app/src/main/java/com/example/refood/
├── data/
│   ├── remote/        # Retrofit: APIs, DTOs, NetworkModule
│   ├── repository/    # Implementaciones remotas sobre las APIs
│   └── session/       # SessionManager (DataStore)
├── di/                 # AppContainer y ViewModelFactory
├── domain/
│   ├── model/          # Modelos de dominio (Product, Order, User, CartLine, ...)
│   └── validation/     # Validadores de formularios (FieldValidators)
├── navigation/         # Rutas y NavGraph
├── notifications/      # Firebase Cloud Messaging
├── ui/
│   ├── components/     # Componentes reutilizables (ProductCard, StatusChip, ...)
│   ├── screens/        # Pantallas + ViewModels por módulo (auth, home, products, cart, order, orders, profile)
│   └── theme/          # Color, tipografía y formas de la marca ReFood
├── MainActivity.kt
└── ReFoodApplication.kt
```
