# Dungeon

## 1. Descripción general

La dungeon se encuentra dentro de la dimensión **Tierras Profundas** y está formada por diferentes salas y niveles de dificultad.

El jugador debe avanzar por las distintas salas, derrotar a los enemigos y conseguir las **dos tarjetas amarillas** necesarias para fabricar la **tarjeta roja**.

La tarjeta roja permite acceder a la zona donde se encuentra **MR Mullet**.

El recorrido general es:

```text
Entrada
   ↓
Fase 1
   ↓
Tarjeta amarilla
   ↓
Fase 2
   ↓
Tarjeta amarilla
   ↓
Tarjeta roja
   ↓
Entrada a MR Mullet
   ↓
MR Mullet
   ↓
Salida
```

---

## 2. Diseño de la dungeon

La dungeon estará formada por:

- Una entrada.
- Una zona correspondiente a la Fase 1.
- Una zona correspondiente a la Fase 2.
- Salas de enemigos.
- Salas o zonas con cofres.
- Dos tarjetas amarillas.
- Una zona de acceso a MR Mullet.
- Sala del jefe.
- Una salida.

La distribución exacta de las habitaciones se encuentra representada en el diagrama de diseño de la dungeon.

### Diagrama

> **Pendiente de agregar imagen del diseño definitivo de la dungeon.**

---

# 3. Fase 1

La Fase 1 está dividida en tres niveles de dificultad.

## Nivel 1

| Enemigo | Cantidad |
|---|---:|
| Zombies | 8 |
| Esqueletos | 5 |

No se especifican armaduras o armas especiales para este nivel.

## Nivel 2

| Enemigo | Cantidad | Características |
|---|---:|---|
| Zombies | 12 | 4 con armadura de hierro |
| Esqueletos | 7 | 3 con arco encantado |

## Nivel 3

| Enemigo | Cantidad | Características |
|---|---:|---|
| Zombies | 16 | 6 con armadura de hierro |
| Esqueletos | 10 | 4 con arco encantado |
| Zombies | 2 | Armadura de diamante |

Al completar la Fase 1 se obtiene una **tarjeta amarilla**.

---

# 4. Fase 2

La Fase 2 aumenta la cantidad y dificultad de los enemigos.

## Nivel 1

| Enemigo | Cantidad | Características |
|---|---:|---|
| Zombies | 12 | 4 con armadura de hierro |
| Esqueletos | 8 | — |

## Nivel 2

| Enemigo | Cantidad | Características |
|---|---:|---|
| Zombies | 16 | 6 con armadura de hierro |
| Esqueletos | 10 | 5 con arco encantado con Empuje 3 |

## Nivel 3

| Enemigo | Cantidad | Características |
|---|---:|---|
| Zombies | 20 | 8 con armadura de hierro, 3 con armadura de diamante |
| Esqueletos | 12 | 6 con arco encantado |

Al completar la Fase 2 se obtiene la segunda **tarjeta amarilla**.

---

# 5. Tarjetas

## Tarjeta amarilla

Existen dos tarjetas amarillas dentro de la dungeon.

Cada una se obtiene después de superar una de las fases principales (encontrándose dentro de los dos cofres de recompensa de la dungeon, una tarjeta por cofre):

- **Cofre 1:** Ubicado tras superar la Fase 1 (contiene 1 Tarjeta Amarilla).
- **Cofre 2:** Ubicado tras superar la Fase 2 (contiene 1 Tarjeta Amarilla).

```text
Fase 1 → Cofre 1 → Tarjeta amarilla
Fase 2 → Cofre 2 → Tarjeta amarilla
```

Las dos tarjetas amarillas se utilizan para fabricar la **tarjeta roja**.

## Tarjeta roja

La tarjeta roja se fabrica utilizando las dos tarjetas amarillas.

```text
Tarjeta amarilla
        +
Tarjeta amarilla
        ↓
   Tarjeta roja
```

La tarjeta roja permite acceder a la zona de **MR Mullet**.

---

# 6. MR Mullet

Después de conseguir la tarjeta roja, el jugador puede acceder a la zona donde se encuentra MR Mullet.

MR Mullet es el jefe principal de la dungeon.

Su sala se encuentra después de las fases de enemigos y cuenta con una entrada y una salida.

### Características del jefe

- Alta resistencia.
- Ataques a distancia.
- Habilidades sobrenaturales.
- Efectos relacionados con el malware.
- Diferentes fases de combate.

Los ataques y estadísticas exactas del jefe se definirán en la documentación correspondiente al jefe.

---

# 7. Loot

El loot de suministros y recompensas generales se encuentra distribuido en los **barriles** de la sala de barriles, mientras que los **dos cofres** principales contienen las **Tarjetas Amarillas** de progresión (una por cofre).

### Características del loot en barriles:
- **Tiradas mínimas garantizadas:** Cada barril genera entre **4 y 6 objetos** (`rolls: 4..6`), evitando que queden vacíos o semivacíos.
- **Mayor cantidad en objetos de valor:** Los minerales y recursos valiosos (que no son herramientas/armaduras) aparecen en mayores cantidades/stacks cuando se generan.
- **Herramientas y equipamiento:** Aparecen de forma individual (1 unidad).

## Tabla de loot (Barriles)

| Objeto | Cantidad por aparición | Probabilidad relativa (peso) |
|---|:---:|---:|
| 8 bloques de adoquín | 8 | 12% |
| Un solo palo | 1 | 12% |
| Tierra | 2 – 6 | 10% |
| Hilo | 1 – 3 | 9% |
| Huesos | 2 – 5 | 9% |
| Carne podrida | 2 – 5 | 9% |
| Grava | 2 – 6 | 8% |
| Papel | 2 – 5 | 8% |
| Semillas de trigo | 2 – 5 | 8% |
| Ojo de araña | 1 – 3 | 7% |
| Tinte gris | 1 – 3 | 7% |
| Un cuenco | 1 | 7% |
| Patata venenosa | 1 | 6% |
| Arena | 2 – 6 | 6% |
| Bota de cuero | 1 | 5% |
| Azada de madera | 1 | 5% |
| Manzana normal | 2 – 4 | 5% |
| Cuerda | 1 | 5% |
| Pico de hierro | 1 | 4% |
| Escudo dañado | 1 | 4% |
| Caña de pescar | 1 | 4% |
| Bloque de musgo | 2 – 6 | 4% |
| Lingote de hierro | 4 – 10 | 3% |
| Lingote de oro | 3 – 8 | 3% |
| Lingote de cobre | 6 – 16 | 3% |
| Zanahoria | 4 – 8 | 3% |
| Botella de miel | 2 – 4 | 3% |
| Armadura de hierro sin encantar | 1 | 2,5% |
| Armadura de diamante sin encantar | 1 | 1,5% |
| Espada de diamante sin encantar | 1 | 1,5% |
| Pico de diamante sin encantar | 1 | 1,5% |
| Esmeraldas | 3 – 7 | 1,5% |
| Libro encantado | 1 | 1% |
| Diamantes | 2 – 5 | 0,8% |
| Manzana dorada | 1 – 3 | 0,5% |
| Plantilla de herrería de centinela | 1 – 2 | 0,5% |
| Plantilla de herrería de dunas | 1 – 2 | 0,5% |
| Plantilla de herrería de costa | 1 – 2 | 0,5% |
| Plantilla de herrería de anfitrión | 1 – 2 | 0,2% |

---

# 8. Distribución del loot

- **Cofres principales (2):** Ubicados como recompensa al completar cada una de las fases de combate (Fase 1 y Fase 2). Cada cofre contiene una Tarjeta Amarilla indispensable para progresar hacia la sala del jefe.
- **Sala de barriles:** Gran sala de almacenamiento que concentra los barriles de la dungeon con el loot de suministros y recursos de la tabla anterior.

---

# 9. Progresión del jugador

El recorrido esperado dentro de la dungeon es:

```text
Entrada
   ↓
Fase 1 - Nivel 1
   ↓
Fase 1 - Nivel 2
   ↓
Fase 1 - Nivel 3
   ↓
Cofre 1 (Tarjeta amarilla)
   ↓
Fase 2 - Nivel 1
   ↓
Fase 2 - Nivel 2
   ↓
Fase 2 - Nivel 3
   ↓
Cofre 2 (Tarjeta amarilla)
   ↓
Fabricar tarjeta roja
   ↓
Acceder a MR Mullet
   ↓
Derrotar a MR Mullet
   ↓
Recompensas
   ↓
Salida
```

---

# 10. Recompensas

Después de derrotar a MR Mullet, el jugador recibirá las recompensas correspondientes a la finalización de la dungeon.

Las recompensas pueden incluir:

- Objetos especiales.
- Loot.
- Recompensas relacionadas con la historia.
- Acceso al portal de regreso.

Las recompensas exactas todavía están **pendientes de definir**.

---

# 11. Elementos pendientes de implementación

| Elemento | Estado |
|---|---|
| Diseño general de la dungeon | En diseño |
| Entrada | Pendiente |
| Fase 1 | Pendiente |
| Fase 2 | Pendiente |
| Sistema de enemigos | Pendiente |
| Sistema de loot | Implementado |
| Tarjeta amarilla | Implementado |
| Tarjeta roja | Implementado |
| Sala de MR Mullet | Pendiente |
| MR Mullet | Pendiente |
| Recompensas | Pendiente |
| Salida | Pendiente |
| Generación de la dungeon | Pendiente |
| Diagrama definitivo | Pendiente |

---

# 12. Notas de desarrollo

La dungeon debe generarse dentro de la dimensión **Tierras Profundas**.

La estructura debe respetar la distribución definida en este documento y mantener el orden de progresión establecido.

Las cantidades de enemigos, equipamiento y probabilidades de loot deben poder modificarse fácilmente durante el desarrollo para permitir ajustes de dificultad y balance.

Las características que todavía no estén definidas deberán marcarse como pendientes en lugar de asumir valores definitivos.
