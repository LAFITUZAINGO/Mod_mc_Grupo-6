# Entidades y Combate

Este documento describe en detalle todas las entidades personalizadas agregadas por el mod, sus estadísticas, comportamientos de inteligencia artificial, mecánicas de combate y animaciones/renderizado.

---

## 1. El Jefe: Mr. Mullet (`MrMulletEntity`)

**Mr. Mullet** es el jefe final de la mazmorra y el antagonista principal de la aventura. Representa la versión corrompida por el malware de Pintaluba del profesor, dotado de habilidades sobrehumanas, ataques mágicos a distancia y furia destructiva.

Se ubica en la gran **arena circular de combate** de las Tierras Profundas, en las coordenadas relativas `(102, 28, 106)` (absolutas: `X: 102, Y: 68, Z: 106`).

### 1.1. Atributos y Estadísticas

| Atributo | Valor en Código | Equivalencia en Juego |
|---|:---:|---|
| **Vida Máxima** | `300.0 HP` | 150 corazones |
| **Daño Cuerpo a Cuerpo** | `9.0` | 4.5 corazones por impacto |
| **Armadura Natural** | `8.0` | 4 pecheras de armadura en la barra |
| **Velocidad de Movimiento Base** | `0.28` | Persecución ágil (multiplicador 1.25× en ataque cuerpo a cuerpo) |
| **Resistencia al Empuje (Knockback)** | `0.8` | 80% de reducción a retroceso |
| **Rango de Persecución (Follow Range)** | `35.0` bloques | Detecta al jugador a 35 metros de distancia |
| **Altura física y de renderizado** | `5.0` bloques | Factor de escala `BOSS_SCALE = 5.0 / 1.8` (~2.78× tamaño normal) |
| **Altura de ojos** | `4.25` bloques | Calculada en base a su postura |

### 1.2. Barra de Jefe (BossBar)
- **Nombre:** `§4§lMr Mullet§r` (rojo oscuro y negrita).
- **Color:** Rojo (`BossBarColor.RED`).
- **Superposición:** Continua (`BossBarOverlay.PROGRESS`).
- **Oscurecimiento de pantalla:** Desactivado (`setDarkenScreen(false)`).
- Se muestra a todos los jugadores que tengan línea de visión o estén en la arena.

### 1.3. Música de Combate
- Al recibir el primer golpe de cualquier jugador en modo supervivencia, Mr. Mullet activa la pista de combate **`mullet:dungeon_combate`** (`SoundSource.RECORDS`, volumen 1.5), reproduciéndose en toda la arena.

### 1.4. Habilidades y Comportamiento

#### A. Estado de Frenesí (Frenzy)
- **Frecuencia:** Se activa de forma periódica cada **15 a 20 segundos** (300 a 400 ticks) mientras esté en combate activo. El primer frenesí ocurre a los **10 segundos** (200 ticks) de iniciado el combate.
- **Duración:** Dura entre **3 y 4 segundos** (60 a 80 ticks).
- **Efectos:**
  - **Velocidad quintuplicada (+400%):** Aplica el modificador `FRENZY_SPEED_MODIFIER` con operación `MULTIPLY_TOTAL` y valor `4.0D` ($1.0 + 4.0 = 5\times$ velocidad base).
  - **Ataques a distancia acelerados:** Su tiempo de recarga entre lanzamientos de cartas se reduce de 3.0–4.5 segundos a **1.5–2.5 segundos**.
  - **Efectos visuales y sonoros:** Emite el sonido de rugido de Devastador (`SoundEvents.RAVAGER_ROAR`) y `ModSounds.JEFE_ATAQUE`. En el cliente genera una estela de partículas de aldeano enfadado (`ANGRY_VILLAGER`) y llamas (`FLAME`).

#### B. Combo de Desgaste (Wither por golpes consecutivos)
- Si Mr. Mullet logra impactar cuerpo a cuerpo al mismo objetivo **dos veces consecutivas dentro de una ventana de 5 segundos** (100 ticks):
  - Aplica el efecto de estado **Wither II** durante **6 segundos** (120 ticks).
  - Genera una nube de partículas de tinta de calamar (`SQUID_INK`) y humo (`SMOKE`), acompañada del sonido del Wither (`SoundEvents.WITHER_SHOOT`).
  - El contador de golpes consecutivos se reinicia tras aplicar el efecto.

#### C. Invocación de "Camejitos" (Minions de apoyo)
- Cuando la vida del jefe desciende al **50% o menos** ($\le 150\text{ HP}$), Mr. Mullet invoca un escuadrón de distracción:
  - Aparecen **5 conejos asesinos** (`Rabbit.Variant.EVIL`) con el nombre personalizado `camejitos`.
  - Aparecen distribuidos en un círculo perfecto de 2.5 bloques de radio alrededor del jefe ($2\pi/5$).
  - Tienen la etiqueta `dungeon_camejitos` y persiguen agresivamente al jugador.
  - **Luminiscencia de minions:** Si solo quedan 3 o menos camejitos vivos en la arena, se les aplica automáticamente el efecto **Luminiscencia (`GLOWING`)** para que el jugador pueda localizarlos fácilmente.

#### D. Unicidad y Persistencia
- Mr. Mullet tiene persistencia garantizada (`setPersistenceRequired()`) y **nunca desaparece por lejanía** (`removeWhenFarAway() == false`).
- El gestor de la mazmorra (`WellPortalManager.ensureMrMulletSpawned()`) y los eventos de entidad aseguran que **únicamente exista un Mr. Mullet con vida en toda la dimensión**. Si se derrota, se marca en `DungeonProtectionData` y no vuelve a aparecer.

---

## 2. Proyectiles de Cartas (`CardProjectileEntity`)

Mr. Mullet ataca a distancia disparando cartas mágicas (proyectiles que reutilizan las texturas y modelos de las tarjetas de acceso).

### 2.1. Propiedades Físicas
- **Gravedad cero:** Vuelan en línea recta perfecta (`getGravity() == 0.0F`), similar a los proyectiles del Wither.
- **Velocidad de vuelo:** 1.4F con dispersión de 1.0F.
- **Inmunidad:** Mr. Mullet es inmune al impacto y a las explosiones de sus propios proyectiles.
- **Desaparición:** Si una carta no impacta contra ninguna entidad o bloque tras **10 segundos** (200 ticks), se autodestruye.

### 2.2. Variantes de Cartas

| Tipo | Probabilidad | Daño | Efecto de Estado | Efecto Secundario | Estela de Partículas | Explosión en Bloque |
|---|:---:|:---:|---|---|:---:|:---:|
| **Tarjeta Amarilla** *(Falta / Amonestación)* | **66.7%** (2 de cada 3) | **7.0 HP** (3.5 corazones) | **Lentitud II** (`MOVEMENT_SLOWDOWN`) por 5 segundos | Ninguno | Partículas de crítico (`CRIT`) | Radio 1.1F (respeta `mobGriefing`) |
| **Tarjeta Roja** *(Expulsión / Letal)* | **33.3%** (1 de cada 3) | **12.0 HP** (6.0 corazones) | **Wither II** por 5 segundos | Prende fuego a la víctima por **4 segundos** | Partículas de llamas (`FLAME`) y humo (`SMOKE`) | Radio 1.6F (respeta `mobGriefing`) |

---

## 3. El Profesor Rescatado: Profesor Mullet (`ProfesorMulletEntity`)

El **Profesor Mullet** es la versión humana original del profesor, liberada del malware de Pintaluba tras la derrota de Mr. Mullet.

### 3.1. Aparición
- **No aparece en la mazmorra.** Se genera en el **Overworld** frente al jugador cuando este utiliza el Bloque de Retorno (`mullet_block`) en la sala de victoria.
- Se posiciona a 2.5 bloques en el ángulo de visión del jugador, emitiendo partículas de tótem (`TOTEM_OF_UNDYING`), aldeano feliz (`HAPPY_VILLAGER`) y el sonido de desafío completado (`UI_TOAST_CHALLENGE_COMPLETE`).
- Si ya existe un Profesor Mullet en un radio de 20 bloques, el sistema evita generar duplicados.

### 3.2. Características y Estadísticas
- **Tipo de entidad:** Criatura pacífica (`PathfinderMob`).
- **Modelo:** Humanoide estándar (altura 1.8 bloques) con la textura personalizada `textures/entity/jefe-profe.png`.
- **Vida:** 20.0 HP (10 corazones).
- **Armadura:** 2.0 puntos.
- **Velocidad:** 0.25.
- **Inmunidad al daño:** No puede recibir daño de jugadores que se encuentren en modo supervivencia (`hurt()` cancelado).

### 3.3. Interacción Social (Clic Derecho)
Al interactuar con él usando la mano principal, emite partículas de corazón (`HEART`), reproduce el sonido de aldeano (`SoundEvents.VILLAGER_YES`) y envía uno de sus cuatro diálogos aleatorios al chat del jugador (con un enfriamiento de 2 segundos entre interacciones):

1. `§6§l[Profesor Mullet]§r §f¡Muchas gracias por salvarme! Ese malware casi acaba conmigo... ¡Te debo la vida!`
2. `§6§l[Profesor Mullet]§r §f¡Por fin libre de la corrupción de las Tierras Profundas! Gracias por rescatarme.`
3. `§6§l[Profesor Mullet]§r §f¡Eres un verdadero héroe! Pensé que nunca volvería a ver el Overworld.`
4. `§6§l[Profesor Mullet]§r §fPintaluba no se saldrá con la suya ahora que estoy a salvo.`
