# Stack Tecnológico

Este documento describe todas las tecnologías, plataformas, herramientas, compiladores y utilidades de software empleadas en la creación, compilación y mantenimiento de **Mod_mc_Grupo-6**.

---

## 1. Entorno de Ejecución y Lenguajes

| Tecnología | Versión | Utilización en el Proyecto |
|---|:---:|---|
| **Java** | `17` (OpenJDK / Eclipse Temurin) | Lenguaje base del mod. Configurado en el toolchain de Gradle (`JavaLanguageVersion.of(17)`) requerido por la versión de Minecraft. |
| **Minecraft** | `1.20.1` | Versión objetivo del juego base sobre la que se ejecutan los bloques, entidades, dimensiones y lógica. |
| **Minecraft Forge** | `47.4.28` | API y cargador de mods principal (*Mod Loader*). Proporciona los buses de eventos diferidos (`DeferredRegister`), registros de entidades/sonidos/bloques y ganchos de eventos del servidor. |

---

## 2. Herramientas de Construcción y Automatización

| Herramienta | Versión | Utilización en el Proyecto |
|---|:---:|---|
| **Gradle** | `8.8` | Sistema de compilación y empaquetado automatizado. Gestiona las tareas de construcción del JAR, descarga de assets de Mojang y compilación Java. |
| **Gradle Wrapper** | `8.8` | Scripts ejecutables (`gradlew` y `gradlew.bat`) que aseguran que todos los miembros del equipo utilicen la misma versión idéntica de Gradle sin requerir instalación previa. |
| **ForgeGradle** | `[6.0, 6.2)` | Plugin oficial de Forge para Gradle. Se encarga de la desofuscación de Minecraft, parcheo de clases y preparación de los entornos de ejecución (`runClient`, `runServer`, `runData`). |
| **Mojang Mappings** | `official` (1.20.1) | Nombres oficiales de métodos y campos de Mojang usados para el mapeo durante el desarrollo. |

---

## 3. Control de Versiones y Gestión

| Herramienta | Utilización en el Proyecto |
|---|---|
| **Git** | Sistema de control de versiones distribuido para el seguimiento de cambios, ramas y commits del equipo. |
| **GitHub** | Plataforma de alojamiento remoto del repositorio colaborativo ([`LAFITUZAINGO/Mod_mc_Grupo-6`](https://github.com/LAFITUZAINGO/Mod_mc_Grupo-6)), gestión de ramas, releases y revisiones de código. |

---

## 4. Entornos de Desarrollo (IDEs) y Modelado

| Herramienta | Utilización en el Proyecto |
|---|---|
| **IntelliJ IDEA** | Entorno de desarrollo integrado (IDE) principal con soporte nativo de configuración para ForgeGradle (`genIntellijRuns`). |
| **Visual Studio Code** | Editor de código complementario para documentación en Markdown, inspección de JSONs y scripting. |
| **Blockbench** | Programa de modelado y texturizado 3D utilizado para el diseño y exportación del modelo humanoide y texturas de **Mr. Mullet** (`jefe.png`) y el **Profesor Mullet** (`jefe-profe.png`). |

---

## 5. Inteligencias Artificiales de Apoyo al Desarrollo

| Herramienta | Función Desempeñada |
|---|---|
| **Gemini** | Asistencia en la generación de código Java, estructura general y búsqueda de referencias de la API de Forge 1.20.1. |
| **ChatGPT** | Redacción y corrección de borradores iniciales de documentación Markdown y diagramas de flujo. |
| **Claude** | Análisis de lógica de sistemas internos de la mazmorra y depuración de algoritmos de barreras. |
| **Antigravity** | Auditoría profunda del código fuente, resolución de discrepancias técnicas entre código y documentación, y sincronización git. |
