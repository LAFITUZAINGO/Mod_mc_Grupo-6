# Mod_mc_Grupo-6: Mullet Pozo

---
> **Autores:** Manuel Pintaluba, Leandro Alonso, Sofía Cedrés, Rodrigo Alonso y Fabián Guillermo  
> **Plataforma:** Minecraft Forge (1.20.1)  
> **Licencia:** All Rights Reserved (Grupo 6)
---

## Descripción General

**Mod_mc_Grupo-6** es un mod de aventura, exploración y combate centrado en una historia narrativa. El objetivo del jugador es adentrarse en la misteriosa dimensión de las **Tierras Profundas** para rescatar al **Profesor Mullet**, quien quedó atrapado y fue corrompido por un malware programado por su estudiante Pintaluba, transformándose en el colosal jefe **Mr. Mullet**.

A lo largo de la travesía, el jugador deberá:
1. Encontrar la estructura de **El Pozo** en el Overworld.
2. Fabricar la **Llave Moneda** y activar el portal.
3. Atravesar las salas de combate y el laberinto de la mazmorra subterránea superando oleadas de enemigos.
4. Obtener las dos **Tarjetas Amarillas** para craftear la **Tarjeta Roja**.
5. Enfrentarse en un combate épico contra **Mr. Mullet**.
6. Liberar al **Profesor Mullet** de la corrupción y regresar sano y salvo al Overworld.

---

## Documentación Técnica y del Proyecto

La documentación oficial completa del mod se encuentra organizada en la carpeta [`Docs/`](Docs/):

- [**Mecánicas y Progresión del Mod**](Docs/Mecanicas.md): Historia, libro inicial "Mr Mullet", ciclo de juego y sistema de tarjetas.
- [**La Mazmorra (Dungeon)**](Docs/Dungeon.md): Mapa arquitectónico, barreras invisibles, reglas anti-grief, luminiscencia y tabla matemática de botín.
- [**Entidades y Combate**](Docs/Entidades_y_Combate.md): Mr. Mullet (frenesí, camejitos, wither), proyectiles de cartas y el Profesor Mullet rescatado.
- [**El Pozo y la Dimensión**](Docs/Pozo_y_Dimension.md): Generación del pozo, activación del portal, propiedades de Tierras Profundas y bloque de retorno.
- [**Comandos Administrativos**](Docs/Comandos.md): Guía y ejemplos de uso de los comandos `/mullet` para pruebas y operadores.
- [**Stack Tecnológico**](Docs/Stack.md): Especificaciones de Java 17, Forge 47.4.28, Gradle 8.8, Blockbench e IAs de apoyo.
- [**Bitácora de Desarrollo e IA**](Docs/bitacora.md): Registro de sesiones, prompts, correcciones realizadas y aprendizajes.

---

## Requisitos e Instalación

### Requisitos:
- **Minecraft:** `1.20.1`
- **Cargador de Mods:** Minecraft Forge `47.4.28` o superior.
- **Java:** Java 17 (64-bit).

### Para Jugadores:
1. Descarga el archivo compilado `mullet-0.3-alpha.jar` directamente desde [Modrinth](https://modrinth.com/mod/mr.mullet).
2. Colócalo dentro de tu carpeta `.minecraft/mods/`.
3. Inicia el launcher seleccionando el perfil de Forge 1.20.1.

### Para Desarrolladores (Compilación desde código fuente):
```bash
# Clonar el repositorio
git clone https://github.com/LAFITUZAINGO/Mod_mc_Grupo-6.git
cd Mod_mc_Grupo-6/Codigo-Mod

# Compilar y empaquetar el mod
./gradlew build
# El JAR resultante se genera en build/libs/mullet-0.3-alpha.jar

# Probar en entorno de desarrollo
./gradlew runClient
```

---

## Equipo y Distribución de Tareas ("Quién hizo qué")

| Integrante | Rol Principal | Tareas / Módulos Desarrollados |
| :--- | :--- | :--- |
| **Manuel Pintaluba** | Arquitectura & Programación Principal (Vibecoder) | Desarrollo del núcleo en Java (eventos de servidor, gestor de la mazmorra, portales NBT y comandos administrativos). Coordinación de la documentación técnica. |
| **Leandro Alonso** | Diseñador de Niveles & Constructor | Construcción integral in-game de las estructuras de la aventura: El Pozo en el Overworld y las salas temáticas de la mazmorra subterránea. |
| **Sofía Cedrés** | Programación, QA & Testing (Vibecoder) | Pruebas de integración in-game, depuración de mecánicas de combate, compilación de binarios para Modrinth y redacción de especificaciones. |
| **Rodrigo Alonso** | Diseñador Gráfico & Técnico de Audio | Creación y modelado de texturas para ítems y bloques. Producción, normalización y conversión de efectos de sonido mono .ogg. Soporte en construcción. |
| **Fabián Guillermo** | Director de Arte & Narrativa | Guión del lore in-game (Mr. Mullet, Pintaluba y el libro introductorio). Redacción de textos, diálogos y control de coherencia artística general. |

> *Para el registro paso a paso de las sesiones con asistentes de Inteligencia Artificial, consulta la [Bitácora de IA](Docs/bitacora.md).*

