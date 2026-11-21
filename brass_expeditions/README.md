# Brass Expeditions — estado de desarrollo

Este subproyecto implementa solo la primera capa del mod; no es todavía una campaña completa ni un JAR aprobado para el modpack.

## Lo implementado

- Seguimiento autoritativo del servidor: cada 40 ticks examina únicamente el bioma/dimensión actuales; los cambios de dimensión se registran en el evento del servidor, así que una visita corta no se pierde. No hay barridos del mundo.
- Progreso aislado por mundo y UUID de jugador, guardado en `data/brass_expeditions.json` dentro del mundo.
- Comando de consulta personal: `/expeditions`.
- Tres hitos iniciales verificables por servidor: Surveyor (biomas distintos), Far Horizons (dimensiones distintas) y Windborne Surveyor (distancia volada en élitros, acumulada solo mientras el servidor observa al jugador en estado de planeo). Los IDs de dimensión se leen de claves registradas; cualquier dimensión modded puede contar.
- Umbrales y premios de ítem opcionales ajustables con la configuración `SERVER` de NeoForge. Si se configura un ítem inexistente, no se entrega y se escribe una advertencia en el log.

## Configuración

NeoForge crea la configuración de servidor `brass_expeditions-server.toml` en `serverconfig/`. Valores disponibles:

- `surveyBiomesGoal` (predeterminado 10)
- `visitDimensionsGoal` (predeterminado 3)
- `elytraFlightBlocksGoal` (predeterminado 5000; avance en bloques aproximados basado en ticks de planeo observados)
- `surveyRewardItem` y `surveyRewardCount` (predeterminado: premio desactivado)
- `dimensionsRewardItem` y `dimensionsRewardCount` (predeterminado: premio desactivado)
- `elytraRewardItem` y `elytraRewardCount` (predeterminado: premio desactivado)

Reinicia el servidor tras modificar el archivo si NeoForge no recarga esa configuración. Un premio se registra como completado una sola vez por UUID y mundo; los objetos que no caben en el inventario se dejan caer junto al jugador. En este prototipo, un cierre abrupto entre guardar el hito y entregar el objeto podría perder el premio, aunque no duplica intencionadamente la entrega.

## Pendiente

Campañas con etapas y objetivos definidos por datos, hallazgos, vuelo, mensajes de introducción, pruebas anti-exploit, comprobación del guardado y entrega de premios dentro de un servidor, y pruebas con Northstar y otros mods de dimensiones. La compilación CI no sustituye estas pruebas.
