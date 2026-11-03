# El Pozo y la Dimensión Tierras Profundas

Este documento detalla la estructura del Pozo en el Overworld, la activación del portal, las especificaciones técnicas de la dimensión personalizada **Tierras Profundas** y el mecanismo de regreso al Overworld.

---

## 1. La Estructura del Pozo (`PozoStructure`)

El Pozo es la estructura de acceso principal que conecta el Overworld con la dimensión de la mazmorra.

### 1.1. Configuración de Generación en el Mundo
Se define en los archivos de datos de generación de mundo (`worldgen`):

- **Archivo de conjunto de estructuras:** `data/mullet/worldgen/structure_set/pozo.json`
- **Archivo de estructura:** `data/mullet/worldgen/structure/pozo.json`
- **Etiqueta de biomas:** `#mullet:has_structure/pozo` (biomas terrestres del Overworld).

| Parámetro | Valor | Descripción |
|---|:---:|---|
| `type` | `minecraft:random_spread` | Algoritmo de dispersión aleatoria por cuadrículas de chunks |
| `spacing` | **80** | Distancia promedio en chunks entre posibles intentos de generación |
| `separation` | **30** | Distancia mínima garantizada en chunks entre pozos |
| `salt` | `48291048` | Semilla matemática única del mod para la dispersión |
| `weight` | `1` | Peso de selección de la estructura dentro del conjunto |

### 1.2. Lógica de Emplazamiento y Enterrado (`PozoStructure.java` & `PozoPiece.java`)
Para evitar que el pozo quede flotando en laderas o aparezca bajo el agua:
1. **Evaluación de desniveles:** El generador evalúa la altura de suelo sólido (`Heightmap.Types.OCEAN_FLOOR_WG`) en el centro y en las cuatro esquinas del perímetro (a una distancia de $\pm 6$ bloques en X y Z).
2. **Nivel del mar:** Si la altura calculada `groundY` está por debajo o igual al nivel del mar (`seaLevel`), la estructura se descarta para evitar que se genere inundada en océanos o ríos.
3. **Enterrado de la base:** La pieza (`PozoPiece`) coloca la estructura `structures/pozo.nbt` enterrándola **6 bloques por debajo del suelo mínimo** (`groundY - 6`). Esto asegura que el foso subterráneo del pozo quede completamente sepultado en la roca y la parte superior armonice con la superficie.
4. **Construcción manual / Administrativa:** El comando `/mullet pozo [entierro]` permite construir la estructura en la posición del jugador, con una profundidad de enterrado configurable de 0 a 15 bloques (por defecto 6).

---

## 2. Activación del Portal

### 2.1. Crafteo de la Llave Moneda (`mullet:moneda`)
Para abrir el portal se requiere fabricar una **Moneda**:
- **Ingredientes:** 1 Esmeralda y 4 Lingotes de Oro.
- **Forma:** Crafteo en mesa en forma de cruz (la esmeralda en el centro y los 4 lingotes de oro en cruz alrededor).

```text
       [ Oro ]
[ Oro ][ Esmeralda ][ Oro ]
       [ Oro ]
```

### 2.2. Proceso de Apertura (`WellPortalManager.tickCoins`)
1. El jugador arroja la moneda al agua del interior del pozo.
2. Cada tick (`WellPortalEvents.onEntityJoinLevel` y `tickCoins`), el mod rastrea los ítems de moneda flotantes.
3. **Validación del Pozo (`isWellWater`):**
   - Comprueba que la lámina de agua sea un área delimitada horizontalmente entre $2\times2$ y $4\times4$ (el pozo estándar tiene un pozo de agua de $3\times3$).
   - Comprueba que el bloque del fondo del pozo sea uno de los bloques estructurales válidos: `black_stained_glass`, `stone_bricks`, `deepslate_bricks`, `cracked_stone_bricks` o `mossy_stone_bricks`.
4. **Transformación:**
   - La moneda arrojada se consume y elimina (`coin.discard()`).
   - Toda la columna de agua se sustituye instantáneamente por bloques de portal del End (`Blocks.END_PORTAL`).
   - Se reproducen los sonidos de apertura de portal del End (`SoundEvents.END_PORTAL_SPAWN`) y trueno lejano (`SoundEvents.LIGHTNING_BOLT_THUNDER`).
   - Se generan partículas de dragón (`DRAGON_BREATH`), destellos (`FLASH`) y portal (`PORTAL`).
   - Se envía un mensaje en el chat a todos los jugadores cercanos a 64 metros:
     `§5§l[Pozo Mullet]§r §d¡El sello se ha roto! El portal a las Tierras Profundas está activo.`

---

## 3. Teletransportación e Ingreso

### 3.1. Interceptación del Portal del End (`onTravelToDimension`)
Dado que el portal del pozo utiliza bloques `Blocks.END_PORTAL` para obtener el efecto visual de vacío estelar:
- Normalmente, Minecraft enviaría al jugador a la dimensión vainilla de `The End`.
- El evento `WellPortalEvents.onTravelToDimension` intercepta el cambio de dimensión: verifica si en un radio de $\pm 4$ bloques existen marcos de portal del End (`Blocks.END_PORTAL_FRAME`).
- Si **no** hay marcos (como en el pozo), **cancela el viaje al End** y desvía al jugador a `mullet:tierras_profundas`.
- Adicionalmente, `WellPortalEvents.onPlayerTick` detecta el contacto físico en cuanto los pies del jugador tocan el portal, teletransportándolo de inmediato sin que caiga al fondo del pozo.

### 3.2. Proceso de Entrada a Tierras Profundas (`teleportToDungeonDimension`)
1. **Guardado del punto de retorno:** Se almacenan las coordenadas de superficie del Overworld en los datos persistentes del jugador (`MulletReturnX`, `MulletReturnY`, `MulletReturnZ`).
2. **Fundido negro cinematográfico:** Aplica 40 ticks (2 segundos) de Ceguera (`BLINDNESS`) y Oscuridad (`DARKNESS`) para cubrir el renderizado inicial de chunks.
3. **Punto de aparición en la Mazmorra:** El jugador aparece sobre la cruz de amatista de la sala inicial de la dungeon:
   - Coordenadas de aparición: `X: 261.5, Y: 69.0, Z: 35.5`, con orientación visual (`Yaw`) hacia el oeste ($90^\circ$).
4. Se reproduce el sonido personalizado **`mullet:dimension_entrar`** y el sonido de viaje de portal.

---

## 4. Especificaciones de la Dimensión "Tierras Profundas"

La dimensión tiene identificador oficial **`mullet:tierras_profundas`**.

### 4.1. Configuración de Dimensión (`dimension/tierras_profundas.json`)
- **Tipo de generador:** `minecraft:flat` (plano vacío).
- **Bioma asignado:** `minecraft:the_void` (sin generación de fauna o vegetación natural).
- **Capas:** 1 única capa de aire (`minecraft:air`), lo que produce un vacío completo donde la única estructura sólida es la plantilla `structures/dungeon.nbt` colocada en el origen `(0, 40, 0)`.

### 4.2. Propiedades del Tipo de Dimensión (`dimension_type/tierras_profundas.json`)

| Propiedad | Valor | Efecto en Juego |
|---|:---:|---|
| `fixed_time` | **18000** | Tiempo congelado en la medianoche eterna (sin ciclo día/noche) |
| `ambient_light` | **0.1** | Iluminación ambiental mínima; oscuridad densa |
| `effects` | `minecraft:the_end` | Efectos atmosféricos, cielo oscuro y niebla del End |
| `has_skylight` | `false` | Sin luz solar desde el cielo |
| `has_ceiling` | `false` | Sin techo de bedrock |
| `bed_works` | `false` | **Las camas explotan** si el jugador intenta dormir |
| `respawn_anchor_works` | `false` | Las anclas de reaparición no funcionan |
| `has_raids` | `false` | No pueden ocurrir invasiones de aldea |
| `min_y` | `0` | Límite inferior del mundo |
| `height` / `logical_height` | `256` | Altura máxima de la dimensión |

---

## 5. El Bloque y Retorno al Overworld (`mullet_block`)

### 5.1. El Bloque Pozo Mullet (`PozoBlock`)
- **Registro:** Bloque `mullet:mullet_block` (derivado de propiedades de bloque de hierro, dureza 3.0, sonido de piedra y nivel de emisión de luz 12).
- **Ubicación:** Se encuentra ubicado exclusivamente en la **Sala Negra de Victoria** tras derrotar a Mr. Mullet, en la posición local `(101, 28, 180)` (absoluta: `X: 101, Y: 68, Z: 180`).
- **Protección anti-rotura:** El bloque no puede ser destruido. Si un jugador intenta golpearlo, recibe un mensaje indicándole que haga clic derecho para regresar.

### 5.2. Mecánica de Regreso (`teleportBackToOverworld`)
Al hacer clic derecho en el `mullet_block` con la mano principal:
1. El jugador es teletransportado de vuelta al Overworld, reapareciendo en sus coordenadas guardadas exactas al lado del pozo original (`MulletReturnX/Y/Z`).
2. Se activa un **enfriamiento de 3 segundos** (`MulletPortalCooldown`) para evitar que el jugador vuelva a entrar al portal accidentalmente.
3. Se reproduce el sonido personalizado **`mullet:dimension_salir`** y partículas de portal.
4. **Aparición del Profesor Rescatado:** Se ejecuta `spawnRescuedProfessor()`, invocando a la entidad pacífica del **Profesor Mullet** frente al jugador, festejando con efectos de victoria y diálogos de agradecimiento.
