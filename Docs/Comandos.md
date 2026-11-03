# Comandos Administrativos

El mod cuenta con un árbol de comandos integrados bajo el comando base `/mullet`. Estos comandos están diseñados para la gestión, depuración, pruebas y administración del mod durante el juego.

Todos los comandos requieren **nivel de permiso de operador 2** (nivel estándar para comandos de trampas/administrador).

---

## 1. Índice de Comandos

| Comando | Descripción |
|---|---|
| [`/mullet libro`](#1-recuperar-el-libro-mullet-libro) | Entrega el libro inicial del mod al jugador. |
| [`/mullet pozo [entierro]`](#2-construir-el-pozo-mullet-pozo-entierro) | Genera la estructura del pozo en la posición del jugador. |
| [`/mullet dungeon`](#3-acceso-rápido-a-la-mazmorra-mullet-dungeon) | Teletransporta al jugador a la entrada de la dungeon. |
| [`/mullet overworld`](#4-regreso-al-overworld-mullet-overworld) | Teletransporta al jugador de vuelta al Overworld. |
| [`/mullet boss`](#5-gestión-del-jefe-mullet-boss) | Resetea y coloca a Mr. Mullet en la arena circular. |
| [`/mullet mobs`](#6-gestión-de-enemigos-mullet-mobs) | Genera o regenera todos los monstruos de Fase 1 y 2. |
| [`/mullet barreras <subcomando>`](#7-gestión-de-barreras-mullet-barreras) | Controla y monitorea las barreras entre salas. |
| [`/mullet sonido <tipo>`](#8-prueba-de-sonidos-mullet-sonido) | Reproduce los efectos de sonido personalizados del mod. |

---

## 2. Detalle de Comandos

### 2.1. Recuperar el Libro: `/mullet libro`
- **Función:** Genera y entrega en el inventario del jugador el libro escrito oficial `"Mr Mullet"` con sus 5 páginas de historia y directrices.
- **Manejo de inventario:** Si el inventario del jugador está lleno, el libro se arroja al suelo junto a él.
- **Mensaje:** `Has recuperado el libro Mr Mullet.`

### 2.2. Construir el Pozo: `/mullet pozo [entierro]`
- **Argumento opcional:** `entierro` (Entero de `0` a `15`, valor por defecto: `6`).
- **Función:** Invoca la plantilla `structures/pozo.nbt` en la posición actual del jugador, calculando el terreno mínimo y enterrando la base la cantidad de bloques especificada.
- **Ejemplos:**
  - `/mullet pozo` $\rightarrow$ Construye el pozo enterrado 6 bloques.
  - `/mullet pozo 8` $\rightarrow$ Construye el pozo enterrado 8 bloques.

### 2.3. Acceso Rápido a la Mazmorra: `/mullet dungeon`
- **Función:** Teletransporta al jugador directamente a la sala inicial con la cruz de amatista en las Tierras Profundas (`X: 261.5, Y: 69.0, Z: 35.5`), orientándolo hacia el pasillo oeste (`Yaw: 90°`).
- **Comportamiento adicional:** Si la estructura de la dungeon aún no fue generada en la dimensión, la coloca automáticamente en `(0, 40, 0)`.

### 2.4. Regreso al Overworld: `/mullet overworld`
- **Función:** Teletransporta al jugador de vuelta a su posición guardada en el Overworld (junto al pozo que utilizó para entrar) o al spawn mundial si no hay punto previo registrado.
- **Activación:** Activa la cinemática de victoria y rescate del **Profesor Mullet**.

### 2.5. Gestión del Jefe: `/mullet boss`
- **Función:** 
  1. Marca `MrMulletDefeated = false` y `MrMulletSpawned = false` en `DungeonProtectionData`.
  2. Si ya existía un Mr. Mullet en otra posición, lo reubica en el centro de la arena circular `(102, 68, 106)`.
  3. Si no existía ninguno o había sido derrotado, genera una nueva instancia fresca del jefe.
- **Mensaje:** `§aBoss Mr Mullet posicionado en la sala circular.`

### 2.6. Gestión de Enemigos: `/mullet mobs`
- **Función:** Ejecuta la generación completa de todos los monstruos de Fase 1 (N1, N2, N3) y Fase 2 (N1, N2, N3) en sus coordenadas exactas, asignándoles sus armaduras, encantamientos de arcos y tags de seguimiento de sala.
- **Mensaje:** `§aMobs de la dungeon generados exitosamente en Fase 1 y Fase 2.`

---

### 2.7. Gestión de Barreras: `/mullet barreras <subcomando>`

Permite inspeccionar y administrar el estado de las barreras invisibles que dividen las salas de la mazmorra.

#### A. `/mullet barreras colocar`
- Coloca bloques invisibles de barrera (`Blocks.BARRIER`) en todas aquellas salas que todavía **no hayan sido despejadas** según los datos guardados de la dimensión.

#### B. `/mullet barreras reset`
- Limpia el registro de puertas abiertas (`resetClearedGates()`) y restaura físicamente todas las barreras en las 6 salas y alrededor de los dos cofres de recompensa.

#### C. `/mullet barreras info`
- Consulta en tiempo real las entidades vivas y el estado de cada puerta de la mazmorra. Envía un reporte estructurado al chat del jugador:

```text
=== ESTADO DE SALAS Y BARRERAS ===
Fase 1 - Nivel 1: X mobs | Puerta abierta: true/false
Fase 1 - Nivel 2: X mobs | Puerta abierta: true/false
Fase 1 - Nivel 3: X mobs | Cofre 1/Hub abierto: true/false
Fase 2 - Nivel 1: X mobs | Puerta abierta: true/false
Fase 2 - Nivel 2: X mobs | Puerta abierta: true/false
Fase 2 - Nivel 3: X mobs | Cofre 2 abierto: true/false
```

---

### 2.8. Prueba de Sonidos: `/mullet sonido <tipo>`

Permite reproducir y testear individualmente los 8 eventos de sonido del mod en la posición actual del jugador:

| Subcomando | Evento de Sonido | Categoría | Descripción |
|---|---|:---:|---|
| `/mullet sonido tarjeta` | `mullet:tarjeta_poner` | `PLAYERS` | Sonido al insertar tarjetas en cerraduras o puertas. |
| `/mullet sonido entrar` | `mullet:dimension_entrar` | `PLAYERS` | Sonido místico al teletransportarse a Tierras Profundas. |
| `/mullet sonido salir` | `mullet:dimension_salir` | `PLAYERS` | Sonido de regreso al Overworld. |
| `/mullet sonido golpe` | `mullet:jefe_golpe` | `HOSTILE` | Sonido de Mr. Mullet al recibir daño. |
| `/mullet sonido muerte` | `mullet:jefe_muerte` | `HOSTILE` | Sonido de derrota y muerte de Mr. Mullet. |
| `/mullet sonido ataque` | `mullet:jefe_ataque` | `HOSTILE` | Gruñido de ataque de Mr. Mullet al entrar en combate. |
| `/mullet sonido ambiente` | `mullet:dungeon_ambiente` | `RECORDS` | Música ambiental misteriosa de la mazmorra. |
| `/mullet sonido combate` | `mullet:dungeon_combate` | `RECORDS` | Música intensa de combate contra el jefe. |
