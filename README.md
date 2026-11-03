# Mod_mc_Grupo-6: Mullet Pozo

---
> **Autores:** Manuel Pintaluba, Leandro Alonso, Sofía Cedrés, Rodrigo Alonso y Fabián Guillermo  
> **Plataforma:** Minecraft Forge (1.20.1)  
> **Licencia:** All Rights Reserved (Grupo 6)
---

## 📖 Descripción General

**Mod_mc_Grupo-6** es un mod de aventura, exploración y combate centrado en una historia narrativa. El objetivo del jugador es adentrarse en la misteriosa dimensión de las **Tierras Profundas** para rescatar al **Profesor Mullet**, quien quedó atrapado y fue corrompido por un malware programado por su estudiante Pintaluba, transformándose en el colosal jefe **Mr. Mullet**.

A lo largo de la travesía, el jugador deberá:
1. Encontrar la estructura de **El Pozo** en el Overworld.
2. Fabricar la **Llave Moneda** y activar el portal.
3. Atravesar las salas de combate y el laberinto de la mazmorra subterránea superando oleadas de enemigos.
4. Obtener las dos **Tarjetas Amarillas** para craftear la **Tarjeta Roja**.
5. Enfrentarse en un combate épico contra **Mr. Mullet**.
6. Liberar al **Profesor Mullet** de la corrupción y regresar sano y salvo al Overworld.

---

## 📚 Documentación Técnica y del Proyecto

La documentación oficial completa del mod se encuentra organizada en la carpeta [`Docs/`](Docs/):

- 📜 [**Mecánicas y Progresión del Mod**](Docs/Mecanicas.md): Historia, libro inicial "Mr Mullet", ciclo de juego y sistema de tarjetas.
- 🏰 [**La Mazmorra (Dungeon)**](Docs/Dungeon.md): Mapa arquitectónico, barreras invisibles, reglas anti-grief, luminiscencia y tabla matemática de botín.
- ⚔️ [**Entidades y Combate**](Docs/Entidades_y_Combate.md): Mr. Mullet (frenesí, camejitos, wither), proyectiles de cartas y el Profesor Mullet rescatado.
- 🌌 [**El Pozo y la Dimensión**](Docs/Pozo_y_Dimension.md): Generación del pozo, activación del portal, propiedades de Tierras Profundas y bloque de retorno.
- ⚙️ [**Comandos Administrativos**](Docs/Comandos.md): Guía y ejemplos de uso de los comandos `/mullet` para pruebas y operadores.
- 🛠️ [**Stack Tecnológico**](Docs/Stack.md): Especificaciones de Java 17, Forge 47.4.28, Gradle 8.8, Blockbench e IAs de apoyo.

---

## 🚀 Requisitos e Instalación

### Requisitos:
- **Minecraft:** `1.20.1`
- **Cargador de Mods:** Minecraft Forge `47.4.28` o superior.
- **Java:** Java 17 (64-bit).

### Para Jugadores:
1. Descarga el archivo compilado `mullet-1.0.0.jar`.
2. Colócalo dentro de tu carpeta `.minecraft/mods/`.
3. Inicia el launcher seleccionando el perfil de Forge 1.20.1.

### Para Desarrolladores (Compilación desde código fuente):
```bash
# Clonar el repositorio
git clone https://github.com/LAFITUZAINGO/Mod_mc_Grupo-6.git
cd Mod_mc_Grupo-6/Codigo-Mod

# Compilar y empaquetar el mod
./gradlew build
# El JAR resultante se genera en build/libs/mullet-1.0.0.jar

# Probar en entorno de desarrollo
./gradlew runClient
```
