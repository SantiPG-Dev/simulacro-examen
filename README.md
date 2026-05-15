
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-%23ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21.0.2-0088CC?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Windows"/>
</p>

<h1 align="center">📝 Simulacro de Examen</h1>

<p align="center">
  <b>Aplicación de escritorio para practicar exámenes tipo test</b><br>
  Carga preguntas desde múltiples fuentes, genera simulacros cronometrados y lleva un histórico de resultados.
</p>

<p align="center">
  <i>Desarrollada para estudiantes de CesurFP — Programación, BBDD, Sistemas, y más.</i>
</p>

---

## ✨ Funcionalidades

### 🎯 Simulacros
- Elige una asignatura y lanza un simulacro de **40 preguntas en 40 minutos**
- Las preguntas se muestran en orden aleatorio
- **Feedback inmediato**: al responder, ves si es correcto y cuál era la respuesta
- Temporizador con aviso visual cuando quedan menos de 5 minutos
- Al finalizar: nota, aciertos, fallos, sin responder y tiempo empleado
- **Repaso**: revisa todas las preguntas con las respuestas correctas marcadas

### 📂 Gestión de datos
| Opción | Descripción |
|--------|-------------|
| **Importar** → HTML | Selecciona una carpeta y escanea recursivamente todos los `.html` extraídos de Moodle |
| **Importar** → Excel | Importa preguntas desde otro archivo Excel |
| **Importar** → GitHub | Descarga y parsea HTML directamente desde un repositorio de GitHub |
| **Fix webs local** | Escanea la carpeta de descargas, organiza los `SIMULACRO*.html` por asignatura en `html/{asignatura}/` |
| **Fix webs GitHub** | Sube los `SIMULACRO*.html` organizados por asignatura directamente a tu repositorio de GitHub |
| **Revisar Duplicados** | Detecta y elimina preguntas duplicadas del banco de preguntas |

### ⚙️ Configuración
- Carpeta de descargas
- Carpeta de archivos HTML
- Repositorio GitHub y token de acceso
- Tema claro / oscuro

### 📊 Historial
- Todas las notas guardadas con fecha, asignatura, aciertos y nota
- Media global y por simulacro
- Tabla con códigos de color: verde (>=70%), naranja (>=50%), rojo (<50%)

---

## 🧱 Stack técnico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje principal |
| **JavaFX** | 21.0.2 | Interfaz de usuario |
| **Maven** | 3.9+ | Gestión de dependencias y build |
| **Apache POI** | 5.2.5 | Lectura/escritura de archivos Excel (.xlsx) |
| **Gson** | 2.10.1 | Serialización JSON del historial |
| **Jsoup** | 1.17.2 | Parseo de archivos HTML (exportaciones Moodle) |

---

## 📦 Requisitos

- **Java 21** o superior ([descargar](https://adoptium.net/))
- **Maven** 3.9+ ([descargar](https://maven.apache.org/download.cgi))
- **Git** (opcional, para usar la funcionalidad GitHub)

## 🚀 Compilar y ejecutar

```bash
# Compilar
mvn clean package

# Ejecutar (JAR autónomo con todas las dependencias)
java -jar target/simulacro-examen-1.0.0.jar
```

## 📁 Estructura del proyecto

```
simulacro-examen/
├── excel/                          # Banco de preguntas (BateriaPreguntas.xlsx)
│   └── BateriaPreguntas.xlsx
├── html/                           # Archivos HTML organizados por asignatura
│   ├── Acceso a datos/
│   ├── Programacion/
│   └── ...
├── src/
│   └── main/
│       ├── java/com/examenes/
│       │   ├── controller/         # Controladores JavaFX
│       │   ├── model/              # Modelos (Question, ExamResult, etc.)
│       │   ├── service/            # Lógica de negocio (Excel, HTML, GitHub)
│       │   └── util/               # Utilidades (ThemeToggle)
│       └── resources/
│           ├── fxml/               # Vistas FXML
│           ├── style.css           # Estilo claro
│           └── dark-theme.css      # Estilo oscuro
├── pom.xml
└── README.md
```

## ⚙️ Configuración

La configuración se guarda en `~/.simulacro-examen/config.properties`.

| Clave | Descripción | Valor por defecto |
|-------|-------------|-------------------|
| `html.folder` | Carpeta de archivos HTML | `html` |
| `downloads.folder` | Carpeta de descargas | *(vacío)* |
| `github.repo` | Repositorio GitHub (usuario/repo) | *(vacío)* |
| `github.token` | Token de acceso GitHub | *(vacío)* |
| `theme` | Tema de la interfaz | `light` |

## 🧪 Formato del Excel

El banco de preguntas usa hojas nombradas por asignatura con el siguiente formato:

| Pregunta | OpcionA | OpcionB | OpcionC | OpcionD | Correcta |
|----------|---------|---------|---------|---------|----------|
| ¿Qué es un objeto en POO? | Una función | Una instancia de una clase | Un tipo primitivo | Un bucle | B |

---

## 🌐 Configurar GitHub para compartir HTML

La aplicación permite compartir los archivos HTML de los simulacros subiéndolos directamente a un repositorio de GitHub. Así todos los compañeros pueden importarlos desde allí.

### 1. Crear un repositorio en GitHub

1. Ve a [github.com/new](https://github.com/new)
2. Ponle un nombre, por ejemplo `simulacro-examen`
3. Déjalo en **público** (para que todos puedan acceder) o **privado** si lo prefieres
4. No marques ninguna opción de inicialización (README, .gitignore, licencia)
5. Pulsa **Create repository**

### 2. Obtener un token de acceso personal

La aplicación necesita un token para autenticarse contra la API de GitHub.

1. Ve a [github.com/settings/tokens](https://github.com/settings/tokens)
2. Pulsa **Generate new token** → **Fine-grained token**
3. Ponle un nombre, por ejemplo `simulacro-examen`
4. En **Repository access** selecciona **Only select repositories** y elige el repositorio que acabas de crear
5. En **Permissions** → **Contents** selecciona **Read and write**
6. Pulsa **Generate token**
7. **Copia el token ahora** — GitHub no lo volverá a mostrar

> ⚠️ **Importante**: el token debe tener permiso de escritura (`Contents: Read and write`) para que la app pueda subir archivos.

### 3. Configurar la aplicación

1. Abre la aplicación y ve a **Configuración**
2. En **Repositorio GitHub** escribe: `tu-usuario/simulacro-examen` (el que creaste en el paso 1)
3. En **Token GitHub** pega el token que copiaste
4. Pulsa **Guardar**

### 4. Compartir HTML con Fix webs GitHub

1. Descarga los archivos HTML de los simulacros desde Moodle o donde los tengas
2. Ponlos en la carpeta de descargas configurada
3. En la aplicación ve a **Datos** → **Fix webs GitHub**
4. La aplicación los subirá automáticamente a `html/{asignatura}/` en tu repositorio

### 5. Los compañeros lo importan

Cada compañero solo necesita:

1. Configurar en **Ajustes** el mismo repositorio: `tu-usuario/simulacro-examen`
2. No necesitan token para solo leer (importar)
3. Ir a **Datos** → **Importar** → **Desde GitHub**
4. Seleccionar la rama `main` y carpeta `html`
5. La aplicación descargará todos los HTML y los incorporará al banco de preguntas

### ¿Cómo sabe la app qué asignatura es cada HTML?

Los archivos deben llamarse empezando por `SIMULACRO`, seguido del nombre de la asignatura. Por ejemplo:

- `SIMULACRO Acceso a datos (examen 1).html` → asignatura: **Acceso a datos**
- `SIMULACRO Programacion (recuperacion).html` → asignatura: **Programacion**

La aplicación extrae automáticamente la asignatura de todo lo que hay entre `SIMULACRO` (o `SIMULACRO DE`) y el primer paréntesis.

---

<p align="center">
  <sub>Hecho con 💻 para estudiantes de CesurFP</sub>
</p>
