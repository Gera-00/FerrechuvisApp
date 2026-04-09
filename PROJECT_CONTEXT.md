# 📱 Proyecto: FerrechuvisApp (Tlapalería - Catálogo de Productos)

## 🧠 Descripción General

Aplicación Android desarrollada en Kotlin con XML, orientada a la gestión y consulta de productos de una tlapalería.

El objetivo principal es permitir:

* Consulta rápida de productos (nombre, código, categoría)
* Visualización de precios
* Apoyo en ventas físicas (tipo catálogo interno)

---

## 🎯 Objetivo de la Primera Versión (MVP)

* CRUD completo de productos
* Consulta optimizada (pantalla principal)
* Manejo de imágenes (galería y cámara)
* Persistencia local con Room (SQLite)

---

## 🏗️ Arquitectura Actual

### 📦 Capas

* **UI**

  * Activities (XML + Kotlin)
  * RecyclerView (Grid de productos)
* **Data**

  * Room Database
  * DAO
  * Entities

---

## 🗂️ Entidades (Room)

### 📦 Producto

```kotlin
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val codigo: String,
    val precio: Double,
    val categoriaId: Int,
    val imagenPath: String? // ruta local del archivo
)
```

---

### 📦 Categoria

```kotlin
@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)
```

---

### 📦 Usuario (simple)

```kotlin
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String
)
```

---

## 🧠 Base de Datos

* Motor: SQLite
* ORM: Room
* Acceso: DAO
* Operaciones:

  * Insert
  * Update
  * Delete
  * Query (búsqueda flexible)

---

## 🔍 Funcionalidad Principal (Core)

### 📱 Pantalla Principal (MainActivity)

* Buscador por:

  * Nombre
  * Código
* Lista de productos (RecyclerView)
* Diseño tipo Marketplace (2 columnas)

---

## 🧩 Adapter

* `ProductoAdapter`

* Usa `RecyclerView + GridLayoutManager`

* Renderiza:

  * Imagen
  * Nombre
  * Código
  * Precio

* Librería:

  * Glide (carga de imágenes locales)

---

## 🖼️ Manejo de Imágenes

### 📌 Estrategia actual

* NO se guardan URIs temporales
* Las imágenes se copian a almacenamiento interno

### 📂 Ubicación

```
/data/data/{app}/files/
```

### 📌 Flujo

#### 📷 Cámara

* Se crea archivo con FileProvider
* Se guarda directamente en almacenamiento interno
* Se almacena ruta absoluta

#### 🖼️ Galería

* Se selecciona imagen
* Se copia a almacenamiento interno
* Se guarda ruta

---

## 🧪 CRUD de Productos

### 📱 Pantalla: ProductoFormActivity

Funciones:

* Crear producto
* Seleccionar imagen
* Tomar foto
* Guardar en base de datos

---

## 🔐 Permisos

```xml
<uses-permission android:name="android.permission.CAMERA"/>
```

* Permiso de cámara solicitado en runtime

---

## ⚠️ Problemas Resueltos

* ❌ Acceso a DB en hilo principal → ✔️ Dispatchers.IO
* ❌ URIs temporales → ✔️ copia a almacenamiento interno
* ❌ Glide sin permisos → ✔️ uso de File
* ❌ Theme AppCompat → ✔️ uso de ComponentActivity

---

## 🚧 Funcionalidades Pendientes

### 🔹 1. Edición de productos

* Cargar datos existentes
* Actualizar registro

### 🔹 2. Eliminación de productos

* Confirmación
* Eliminación de imagen física

---

### 🔹 3. Categorías reales

* Spinner en formulario
* Relación real con productos

---

### 🔹 4. Escaneo de código de barras

* Uso de cámara
* Librería sugerida: ML Kit o ZXing

---

### 🔹 5. Mejoras de UX

* Validaciones de formulario
* Mensajes (Toast / Snackbar)
* Indicadores de carga

---

### 🔹 6. Persistencia avanzada

* Manejo de caché de imágenes
* Limpieza de archivos no usados

---

## 🧠 Decisiones Técnicas

* Kotlin + XML (no Compose)
* Room para persistencia local
* Glide para imágenes
* FileProvider para cámara
* Arquitectura simple (sin MVVM aún)

---

## 🚀 Futuro (escalabilidad)

* Migrar a MVVM
* Agregar ViewModel + LiveData/Flow
* Sincronización con backend
* Login real con roles
* Multiusuario

---

## 🧪 Estado Actual del Proyecto

✔ CRUD básico funcional
✔ Búsqueda funcional
✔ Imágenes funcionando (galería + cámara)
✔ UI base estable

---

## 📌 Notas para desarrollo con Copilot

* Mantener uso de `Dispatchers.IO` para DB
* Evitar URIs de tipo `content://` sin persistencia
* Usar rutas locales (`File`)
* Mantener separación UI / Data
* No introducir Compose (por consistencia actual)

---

## 🛠️ Protocolo de Git Commits

Para mantener un historial limpio y técnico, el agente (IA) y el desarrollador deben seguir estas reglas estrictas:

### 📝 Estructura del Mensaje
| Componente | Regla |
| :--- | :--- |
| **Título** | `prefijo: descripción corta en minúsculas` (Max. 50 carac.) |
| **Cuerpo** | Línea en blanco tras el título + Explicación del **qué** y **por qué**. |
| **Idioma** | Español técnico (directo y sin relleno). |
| **Verbos** | Presente o imperativo (ej. "añade", "corrige", "implementa"). |

### 🏷️ Prefijos Permitidos
*   `feat:` Nuevas funcionalidades (ej. cámara, persistencia Room).
*   `fix:` Corrección de errores o bugs.
*   `ui:` Cambios visuales, Layouts o recursos de diseño (ej. GridLayout).
*   `refactor:` Mejoras al código que no cambian la lógica (ej. renombrado).
*   `chore:` Configuración de Gradle, dependencias o mantenimiento.

### 🚫 Prohibiciones
*   **No** usar palabras corporativas vacías: *"escalar"*, *"detallar"*, *"optimizar"* (sin explicar qué).
*   **No** explicar el código línea por línea en el commit.
*   **No** mezclar lógica de base de datos con cambios de UI en un solo commit.

### 🌟 Ejemplos de Referencia (Golden Examples)

> **Correcto (Funcionalidad):**
> `feat: estructura base MVVM, Room DB y catálogo`
> *Base de datos y arquitectura inicial listas para recibir lógica de negocio.*

> **Correcto (UI + Feature):**
> `feat: búsqueda en Grid y guardado de productos con cámara`
> *Cambio a 2 columnas en catálogo e implementación de captura fotográfica.*