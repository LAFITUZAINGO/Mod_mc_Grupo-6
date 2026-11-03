# Mecánicas y Progresión del Mod

## 1. Introducción

**Mod_mc_Grupo-6** es un mod de aventura, exploración y combate desarrollado para **Minecraft Forge 1.20.1**.

El argumento central sigue el rescate del **Profesor Mullet**, un eminente académico que fue víctima de un malware corruptor programado por su estudiante **Pintaluba**. Como consecuencia, el profesor fue arrastrado a la dimensión de las **Tierras Profundas**, transformándose en una entidad hostil y colosal conocida como **Mr. Mullet**.

El deber del jugador es equiparse, descifrar las pistas, localizar la entrada mística, superar los peligros de la mazmorra subterránea, derrotar a Mr. Mullet y restaurar la paz rescatando al profesor.

---

## 2. Inicio de la Aventura: El Libro "Mr Mullet"

Al ingresar por primera vez al mundo, cada jugador recibe automáticamente en su inventario el libro oficial de la aventura.

### 2.1. Propiedades del Libro
- **Título en juego:** `Mr Mullet` (registrado en `LibroInicio.java`).
- **Autor:** `Mr Mullet`.
- **Tipo de ítem:** Libro escrito (`minecraft:written_book`).
- **Control de entrega única:** El mod guarda una etiqueta persistente en el jugador (`mullet_libro_recibido: true`) para garantizar que se entregue una sola vez.
- **Recuperación:** Si el jugador extravía su libro, cualquier operador puede devolvérselo con el comando [`/mullet libro`](Comandos.md#1-recuperar-el-libro-mullet-libro).

### 2.2. Contenido de las Páginas
1. **Página 1:** Bienvenida a la aventura e introducción a la misión de rescate.
2. **Página 2:** Contexto sobre el malware de Pintaluba e instrucciones para buscar la estructura de **El Pozo** en el Overworld.
3. **Página 3:** Pistas para fabricar la **Llave Moneda** y utilizarla para abrir el portal en las aguas del pozo.
4. **Página 4:** Advertencia sobre los desafíos de las **Tierras Profundas**, la necesidad de superar las salas de combate y fabricar la **Tarjeta Roja**.
5. **Página 5:** El objetivo final: purificar y liberar a Mr. Mullet de la corrupción.

---

## 3. Acceso a las Tierras Profundas

Para ingresar a la dimensión de la mazmorra, el jugador debe completar una secuencia de dos pasos en el Overworld:

### 3.1. Crafteo de la Llave Moneda (`mullet:moneda`)
Se elabora combinando **1 Esmeralda** en el centro y **4 Lingotes de Oro** formando una cruz en la mesa de crafteo:

```text
       [ Oro ]
[ Oro ][ Esmeralda ][ Oro ]
       [ Oro ]
```

### 3.2. Activación del Pozo
1. El jugador explora el mundo hasta encontrar la estructura natural de **El Pozo**.
2. Arroja la **Llave Moneda** al agua del foso central.
3. La moneda es consumida y el agua se transmuta en bloques de portal estelar, abriendo la puerta a las Tierras Profundas.

> *Para conocer todos los detalles sobre la generación del pozo, probabilidades y lógica técnica de teletransportación, consulta [Pozo_y_Dimension.md](Pozo_y_Dimension.md).*

---

## 4. Exploración de la Mazmorra Subterránea

Una vez dentro de las Tierras Profundas, el jugador aparece en la sala inicial de la mazmorra (Sala de la Cruz de Amatista).

### 4.1. Reglas del Entorno (Modo Aventura / Anti-Grief)
La mazmorra cuenta con un sistema de protección integral:
- **No se pueden romper bloques originales** de la dungeon en supervivencia (únicamente bloques colocados por el propio jugador).
- Las explosiones y los pistones no dañan la estructura original.
- Para avanzar entre salas es obligatorio eliminar a todos los monstruos de cada nivel para que desaparezcan las **barreras mágicas invisibles**.
- Cuando en una sala activa quedan **3 o menos enemigos vivos**, estos emiten un resplandor visible a través de las paredes (**Luminiscencia / `GLOWING`**).

> *Para consultar el mapa detallado, distribución de salas, barreras y tabla de botín de los barriles, consulta [Dungeon.md](Dungeon.md).*

---

## 5. El Sistema de Progresión por Tarjetas

El avance hacia el combate definitivo está regulado por un sistema de tarjetas de seguridad:

```text
[ Superar Fase 1 ] ──> Cofre 1 ──> Tarjeta Amarilla (1ª) ┐
                                                         ├──> [ Crafteo ] ──> Tarjeta Roja ──> Desbloquea Sala del Jefe
[ Superar Fase 2 ] ──> Cofre 2 ──> Tarjeta Amarilla (2ª) ┘
```

1. **Tarjeta Amarilla:** Existen 2 en la mazmorra:
   - Una en el Cofre 1 al superar la Fase 1.
   - Otra en el Cofre 2 al superar la Fase 2.
2. **Tarjeta Roja:** Se craftea en mesa sin orden (receta sin forma) combinando las **2 Tarjetas Amarillas**. Permite desbloquear la puerta principal de lana roja que conduce a la arena de combate.

---

## 6. La Batalla Final: Mr. Mullet

Tras franquear la puerta de lana roja y atravesar el pasillo decorativo, el jugador accede a la **Arena Circular**, donde aguarda **Mr. Mullet**.

- Es una entidad de **5 metros de altura** con 300 puntos de vida (150 corazones).
- Cuenta con ataques cuerpo a cuerpo devastadores, ráfagas de proyectiles de cartas mágicas (tarjetas amarillas ralentizantes y rojas explosivas con fuego y Wither), estado de **Frenesí** periódico (con velocidad quintuplicada) y la invocación de una horda de **Camejitos** al 50% de su vida.

> *Para el desglose completo de estadísticas, ataques, proyectiles y fases del combate, consulta [Entidades_y_Combate.md](Entidades_y_Combate.md).*

---

## 7. Victoria y Rescate del Profesor

Al derrotar a Mr. Mullet:
1. El jefe es purificado y la puerta roja de salida de la arena se desbloquea.
2. El jugador accede a la **Sala Negra de Victoria**, donde se encuentra el **Bloque de Retorno** (`mullet_block`).
3. Al interactuar con el bloque:
   - El jugador es teletransportado de vuelta al Overworld junto al pozo original.
   - Frente a él aparece el **Profesor Mullet** en su forma humana pacífica, agradeciendo al jugador mediante diálogos interactivos y celebrando el rescate.

---

## 8. Documentos del Proyecto

- [Dungeon.md](Dungeon.md): Estructura de la mazmorra, coordenadas, barreras, mecánicas anti-grief y tabla de loot.
- [Entidades_y_Combate.md](Entidades_y_Combate.md): Comportamiento de Mr. Mullet, proyectiles de cartas y el Profesor Mullet.
- [Pozo_y_Dimension.md](Pozo_y_Dimension.md): Generación del pozo, activación del portal y propiedades de la dimensión.
- [Comandos.md](Comandos.md): Lista completa de comandos administrativos para gestión y pruebas.
- [Stack.md](Stack.md): Tecnologías, herramientas, librerías y versiones del entorno de desarrollo.
