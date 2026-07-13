# 🥗 ReFood — App de Clientes

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white)
![Architecture](https://img.shields.io/badge/Arquitectura-MVVM-orange)
![Status](https://img.shields.io/badge/Estado-En%20desarrollo-yellow)

**ReFood** es una solución tecnológica que conecta **clientes** y **administradores** para reducir el desperdicio de alimentos, permitiendo publicar y adquirir productos próximos a vencer o excedentes de comida a precios accesibles.

Esta rama (`Willy-ReFood_Movil`) contiene la **aplicación Android de Clientes**, desarrollada en Kotlin con Jetpack Compose y arquitectura MVVM.

> La web de Administradores se desarrolla en paralelo por otro integrante del equipo (rama `Marco-ReFood_Web`).

---

## 📱 Funcionalidades

| Módulo | Descripción |
|---|---|
| **Autenticación** | Registro e inicio de sesión con validación de formato de correo, contraseña y sesión persistente. |
| **Pantalla principal** | Saludo personalizado, ofertas especiales destacadas y productos disponibles. |
| **Productos** | Catálogo completo con búsqueda por nombre y filtro por categoría. |
| **Detalle de producto** | Descripción, precio con descuento, stock, fecha de vencimiento y selector de cantidad. |
| **Ofertas especiales** | Productos próximos a vencer con descuento destacado. |
| **Carrito de compra** | Edición de cantidades, eliminación de ítems y cálculo de total en tiempo real. |
| **Formulario de pedido** | Dirección de entrega, método de pago y notas para el negocio. |
| **Confirmación de pedido** | Resumen de éxito con número de pedido. |
| **Mis pedidos** | Historial de pedidos con estado (pendiente, en preparación, listo, entregado, cancelado). |
| **Detalle de pedido** | Resumen completo de productos, total, entrega y pago. |
| **Perfil** | Edición de datos personales y cierre de sesión. |

---

## 🏗️ Arquitectura

La app sigue **MVVM** organizada por capas, para que la fuente de datos pueda evolucionar de local a remota sin tocar la UI:

```
ui/            Composables (pantallas + componentes reutilizables) y ViewModels
domain/        Modelos de dominio
data/
  local/       Room (SQLite): entidades, DAOs, base de datos y datos de ejemplo
  repository/  Interfaces + implementación (hoy Room, mañana Retrofit)
  session/     Sesión de usuario (DataStore Preferences)
di/            Inyección de dependencias manual (AppContainer + ViewModelFactory)
navigation/    Rutas y grafo de navegación (Navigation Compose)
```

**Persistencia actual:** los datos (usuarios, productos, carrito y pedidos) se almacenan en una base de datos **relacional local (Room/SQLite)**, ya que aún no existe un backend compartido con la web de Administradores. La capa de `repository` está diseñada como una interfaz independiente de la fuente de datos, de modo que al integrar la API REST del backend solo se reemplaza la implementación (por una basada en Retrofit), sin modificar ViewModels ni pantallas.

---

## 🛠️ Stack tecnológico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** con `ViewModel`, `StateFlow` y `Coroutines`
- **Navigation Compose** para la navegación entre pantallas
- **Room** para persistencia local relacional
- **DataStore Preferences** para la sesión del usuario
- **Coil** para carga de imágenes
- Inyección de dependencias manual (`AppContainer`), sin frameworks externos

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos

- **Android Studio** (Ladybug o superior recomendado)
- **JDK 11+** (se recomienda usar el JDK embebido de Android Studio)
- Emulador o dispositivo físico con **Android 7.0 (API 24)** o superior
- Conexión a internet (las imágenes de productos se cargan desde una URL de ejemplo)

### Pasos

1. Clona el repositorio y cambia a esta rama:
   ```bash
   git clone https://github.com/Alex05ander2004/PROYECTO_FINAL_DANP.git
   cd PROYECTO_FINAL_DANP
   git checkout Willy-ReFood_Movil
   ```
2. Abre la carpeta del proyecto en **Android Studio**.
3. Deja que Gradle sincronice las dependencias.
4. Ejecuta la app (▶) sobre un emulador o dispositivo conectado.

La base de datos se crea automáticamente en el primer inicio, precargada con un catálogo de productos de ejemplo (panadería, lácteos, frutas y verduras, abarrotes y bebidas).

---

## 📂 Estructura del proyecto

```
app/src/main/java/com/example/refood/
├── data/
│   ├── local/        # Entidades Room, DAOs, AppDatabase, seed de datos
│   ├── repository/   # Interfaces y su implementación
│   └── session/       # SessionManager (DataStore)
├── di/                # AppContainer y ViewModelFactory
├── domain/model/      # Modelos de dominio (Product, Order, User, CartLine, ...)
├── navigation/        # Rutas y NavGraph
├── ui/
│   ├── components/    # Componentes reutilizables (ProductCard, StatusChip, ...)
│   ├── screens/       # Pantallas + ViewModels por módulo (auth, home, products, cart, order, orders, profile)
│   └── theme/         # Color, tipografía y formas de la marca ReFood
├── MainActivity.kt
└── ReFoodApplication.kt
```

---

## 🔜 Próximos pasos

- Integrar la API REST compartida con la web de Administradores sobre la base de datos relacional.
- Sustituir el hashing local de contraseñas por autenticación gestionada en el backend.
- Sincronizar catálogo de productos y estados de pedido en tiempo real entre ambas plataformas.
