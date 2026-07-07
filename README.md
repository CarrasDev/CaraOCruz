# Cara o Cruz 🪙

**Cara o Cruz** es una aplicación móvil nativa para Android desarrollada como proyecto para la asignatura de **Aplicaciones Nativas** de la **UOC (Universitat Oberta de Catalunya)**. La aplicación ofrece una experiencia de juego de azar clásica, permitiendo a los usuarios competir tanto en solitario (offline) como contra otros jugadores en un entorno global (online).

Este proyecto ha sido diseñado siguiendo los estándares modernos de desarrollo en Android, enfocándose en la modularidad, el uso de recursos multimedia y la integración de servicios en la nube, siendo una pieza clave para demostrar competencias técnicas en un portfolio profesional.

---

## 🚀 Características Principales

### 🎮 Modos de Juego
- **Modo Local (Offline)**: Juego contra la CPU con persistencia de datos local para mantener un historial de partidas y saldo personal.
- **Modo Online (Multijugador)**: Competición global mediante autenticación. Incluye mecánicas de **Bote Acumulado (Jackpot)** sincronizadas en tiempo real.

### 🛠️ Funcionalidades Avanzadas
- **🌍 Geolocalización**: Registro opcional de la ubicación geográfica en el momento de la victoria, permitiendo al usuario recordar dónde tuvo suerte.
- **📅 Integración con Calendario**: Posibilidad de registrar hitos y grandes victorias directamente en el calendario del sistema.
- **📸 Capturas de Pantalla**: Funcionalidad integrada para capturar momentos de victoria y guardarlos automáticamente en la galería del dispositivo.
- **🎵 Personalización Multimedia**: 
    - Gestión de audio mediante `MediaPlayer`.
    - **Selector de Música**: Permite al usuario elegir archivos de audio locales para personalizar la música de fondo del juego.
- **🔔 Sistema de Notificaciones**: Notificaciones locales para informar sobre victorias y actualizaciones del estado del juego.

---

## 🛠️ Stack Tecnológico

La aplicación utiliza las herramientas y bibliotecas más robustas del ecosistema Android:

- **Lenguaje**: [Kotlin](https://kotlinlang.org/) (100% nativo).
- **Arquitectura**: **MVVM (Model-View-ViewModel)**, asegurando una separación clara de responsabilidades y facilidad de mantenimiento.
- **Componentes de Jetpack**:
    - **Navigation Component**: Gestión centralizada de la navegación entre fragmentos.
    - **ViewBinding**: Interacción segura y eficiente con las vistas.
    - **ViewModel & LiveData**: Gestión del estado de la UI orientada al ciclo de vida.
    - **Room Database**: Persistencia de datos local robusta sobre SQLite.
- **Firebase Ecosystem**:
    - **Firebase Authentication**: Gestión segura de usuarios (Login/Registro).
    - **Cloud Firestore**: Base de datos NoSQL para el ranking global y datos en tiempo real.
    - **Cloud Functions**: Lógica de backend escalable para el procesamiento de apuestas.
- **Networking**: **Retrofit 2** para el consumo de servicios web y APIs REST.
- **Concurrencia**: Uso intensivo de **Corrutinas** para operaciones asíncronas no bloqueantes.

---

## 📂 Estructura del Proyecto

El código fuente está organizado siguiendo principios de diseño limpio:

- `ui/`: Fragmentos, ViewModels y Adaptadores organizados por módulos funcionales (juego, ranking, menús).
- `data/`: Repositorios, DAOs, Entidades de Room y servicios de API (Patrón Repository).
- `utils/`: Clases de soporte para gestión de música, notificaciones, autenticación, geolocalización y manejo de medios.
- `functions/`: Código fuente de las Cloud Functions de Firebase (Node.js).

---

## ⚙️ Instalación y Configuración

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/CaraOCruz.git
   ```
2. **Configuración de Firebase**:
   - Descarga el archivo `google-services.json` desde tu consola de Firebase y ubícalo en el directorio `/app`.
   - Asegúrate de habilitar Firestore y Authentication (Email/Google).
3. **Compilación**: Abre el proyecto en Android Studio y sincroniza con Gradle.

---

## 🎓 Contexto Académico

Este proyecto demuestra el dominio de conceptos críticos de la programación nativa en Android:
- Gestión del ciclo de vida de componentes complejos.
- Implementación de **Servicios y Procesos en segundo plano**.
- Integración de **Hardware y Sensores** (GPS, Galería).
- Consumo de **Servicios en la Nube** y persistencia híbrida.

---

**Desarrollado con ❤️ para el Grado de Ingeniería Informática - UOC.**
