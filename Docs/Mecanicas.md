# Mecánicas y Progresión del Mod

## 1. Introducción

**Mod_mc_Grupo-6** es un mod de Minecraft Forge 1.20.1 centrado en la exploración, la aventura y la progresión narrativa.

El objetivo principal del jugador es encontrar y rescatar al profesor **MR_MULLET**, quien quedó atrapado en una dimensión desconocida y fue corrompido por un malware creado por su alumno Pintaluba.

Para completar su misión, el jugador deberá encontrar una estructura especial, acceder a una dimensión, superar diferentes desafíos, conseguir objetos necesarios para avanzar y enfrentarse al jefe final.

## 2. Inicio e Historia

### Libro de la Verdad

Al iniciar el mundo, el jugador recibe automáticamente en su inventario un libro llamado **Libro de la Verdad**.

Este libro cumple las siguientes funciones:

- Introducir la historia principal del mod.
- Explicar la misión de rescatar a MR_MULLET.
- Proporcionar pistas sobre la ubicación del Pozo.
- Orientar al jugador sobre cómo comenzar su aventura.

Si el jugador pierde el libro, puede recuperarlo mediante un comando administrativo.

## 3. Acceso a la Dimensión

### Llave Moneda

La **Llave Moneda** es el objeto utilizado para activar el portal del pozo.

**Receta de crafteo:**

- 1 Esmeralda.
- 4 Lingotes de Oro.

La Esmeralda debe colocarse en el centro de la mesa de crafteo, mientras que los cuatro lingotes de oro deben ubicarse a su alrededor, formando una cruz.

Una vez fabricada, debe arrojarse al agua de El pozo para asi activar el portal.

**Distribución:**

| | | |
|:---:|:---:|:---:|
| | Oro | |
| Oro | Esmeralda | Oro |
| | Oro | |

### Estructura del Pozo

El jugador deberá explorar el mundo hasta encontrar una estructura especial llamada **El Pozo**.

Características:

- Su tasa de aparición será equivalente a la de la estructura que contiene el portal del End.
- Se establecerá una condición para evitar que ambas estructuras aparezcan juntas.
- Funcionará como entrada principal a la dimensión del calabozo.

### Activación del Portal

Para activar el portal, el jugador debe arrojar la Llave Moneda al agua de El Pozo.

Cuando se reconoce que la moneda fue arrojada en la estructura correcta, el objeto desaparece y el agua se transforma en bloques de portal.

Al ingresar, el jugador es transportado a la zona inicial de la dungeon.

## 4. Dimensión Tierras Profundas

La dimensión **Tierras_profundas** estará formada por una estructura subterránea de aspecto rústico, lúgubre y claustrofóbico.

Su construcción utilizará principalmente bloques vanilla de Minecraft, con predominancia de tonalidades grises y distribuciones arquitectónicas irregulares.

El objetivo de esta ambientación es transmitir una sensación de aislamiento, misterio y peligro durante la exploración.

### Distribución del Calabozo

- [Mecánicas y progresión del mod](Docs/Mecanicas.md)

## 5. Sistema de Tarjetas

El sistema de tarjetas es una mecánica de progresión que controla el acceso a determinadas áreas del calabozo.

### Tarjeta Amarilla

Las Tarjetas Amarillas son objetos necesarios para avanzar dentro de la dungeon.

Se encuentran en los dos cofres principales de recompensa:

- El primer cofre contiene una Tarjeta Amarilla.
- El segundo cofre contiene otra Tarjeta Amarilla.

Se necesitan las dos tarjetas para fabricar la Tarjeta Roja.

### Tarjeta Roja

La **Tarjeta Roja** permite desbloquear la entrada a la sala del jefe final.

**Receta de crafteo:**

- 2 Tarjetas Amarillas.

Ambas tarjetas deberán combinarse en una mesa de crafteo sin importar su orden.

Una vez fabricada, el jugador podrá utilizarla para desbloquear la puerta principal de la sala de MR_MULLET.

## 6. Batalla Final: MR_MULLET

### Descripción

**MR_MULLET** es el jefe final del mod y representa el último desafío de la aventura y se encuentra en la sala circular dentro de la dungeon.

Debido a la corrupción provocada por el malware de Pintaluba, el profesor ha adquirido habilidades sobrenaturales y un comportamiento hostil.

### Ubicación

Se encontrará en la sala principal del calabozo, cuyo acceso requiere la Tarjeta Roja.

### Habilidades y Características

Mr. Mullet es el jefe principal de la aventura. Se encuentra en una sala circular dentro de la dungeon.

Es una entidad hostil que persigue y ataca a los jugadores. Cuenta con ataques cuerpo a cuerpo y proyectiles con forma de tarjetas.
Durante el combate puede activar un modo especial llamado Frenesí, que aumenta temporalmente su velocidad de movimiento y modifica la frecuencia de sus ataques.
Cuando su vida llega al 50 %, invoca cinco conejos hostiles llamados camejitos.
El jefe cuenta con una barra de vida propia y no desaparece por alejarse del jugador.

Las estadísticas, los ataques y los efectos de sus proyectiles se encuentran detallados en el documento Jefe.md.


El enfrentamiento representará el desafío más importante de la dimensión.

## 7. Recompensas y Finalización

Al derrotar a MR_MULLET, el jugador habrá completado el objetivo principal de la aventura.
