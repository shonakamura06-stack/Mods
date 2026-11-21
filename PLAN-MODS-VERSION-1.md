# Mods propios — Versión 1: alcance y decisiones

**Estado: diseño aprobado en parte; no son JARs ni builds probados.** Este archivo evita que la implementación invente compatibilidades o anuncie funciones a medias.

## Plataforma fijada

- Minecraft Java **1.21.1**
- NeoForge **21.1.267**
- LWJGL **3.3.3** (dato de la instancia, no dependencia mod)
- Perfil de mods de referencia: las versiones y archivos visibles en las capturas del usuario. La captura de la lista no trae los binarios ni demuestra que el conjunto ya arranque en cliente o servidor.
- Entre los mods que se integrarán opcionalmente están Create 6.0.10, Mekanism 10.7.19.85, AE2 19.2.20, Aeronautics 1.3.3, Sable 2.0.6, Northstar 0.6.6, Waystones 21.1.47 y Progressive/compatibilidad según el manifiesto real. Sus APIs y lados cliente/servidor se comprobarán contra esos archivos antes de compilar.

## Entregables solicitados

Cinco mods independientes, cada uno con su propio código y JAR, bajo el directorio literal `mods version 1/` en GitHub cuando haya una compilación real. Un paquete de recursos separado para interfaz visual se mantendrá fuera de esos cinco JAR. No se incluirán ni redistribuirán JARs de terceros; solo nuestros mods y nuestros recursos originales.

1. **Mod 1 — Registro de Hitos Industriales.** Logros extraordinarios comprobados por eventos/estado de servidor, perfiles individuales visibles para otros jugadores y una interfaz de exhibición. No basta con entregar objetos a un NPC y afirmar que se hizo una fábrica: cada logro publicado debe tener criterio reproducible, evidencia guardada y pruebas anti-fraude. Primera familia candidata: producción de oro automatizada con Create; multiplicación x5 de mena con Mekanism. Antes de anunciar estos dos logros habrá que demostrar que el detector puede observar el flujo real de la receta/maquinaria en el servidor. Los hitos que no puedan detectarse con fiabilidad no se publicarán como automáticos.
2. **Mod 2 — Fábrica Viva.** Diagnóstico y ambiente para estados importantes de Create, Mekanism, AE2 y Aeronautics: actividad/detención, bloqueo, falta de insumos/salida/energía, problemas de canal/red, estado de aeronave cuando exista API fiable. Sonido, luces, partículas y mensajes configurables individualmente; límites de frecuencia, distancia y accesibilidad; nada de barrer el mundo cada tick. Las lecturas no disponibles se omiten, no se simulan.
3. **Mod 3 — Desafíos de Expedición.** Segundo sistema independiente de logros: campañas/retos de exploración, vuelos, hallazgos y progresión de mundo, separado del detector industrial. Retos y premios configurables; el contenido nunca puede exigir un objeto que el jugador necesite para conseguir el propio desbloqueo. La integración con dimensiones modded es explícita.
4. **Mod 4 — Atlas de Frontera (estructuras y habitantes).** Sitios grandes, distintivos y cuidadosamente compuestos: talleres activos/abandonados, bunkers, estaciones técnicas, viviendas pequeñas/medianas/grandes, pueblos periféricos, fortificaciones, refugios y restos de aeronaves. Overworld, Nether, End y Northstar, con paletas, generación y loot apropiados por dimensión. Solo el contenido probado se activa. Generación en chunks nuevos; no aparece retroactivamente en chunks ya explorados.
5. **Mod 5 — Cartografía Create: Pergamino de Latón.** Un mod único de minimapa y atlas completo: mapa de pergamino ilustrado, marco/casing brass, engranajes animados, leyenda, brújula, escala y controles configurables. Waystones se detecta como integración opcional y puede etiquetar sus destinos usando su API/datos reales. Incluir opción accesible para ocultar/eliminar el marcador de muerte. No copiar el arte de las imágenes con marca de agua ni sustituir arte con generación de imágenes.

## Civiles y comercio (decisión de diseño delegado)

- Habitantes con siluetas/ropa y profesiones legibles, mezclados con asentamientos y aldeanos; los encuentros de inicio ayudan sin trivializar Create/Mekanism/AE2.
- Catálogo de comercio curado y limitado por profesión, biome/estructura, etapa y rareza; no “miles” de ofertas aleatorias repetidas. Oferta ilustrativa: una herramienta temprana a cambio de hierro solo tras equilibrar coste, disponibilidad y reparación. Recetas/ítems finales de Mekanism y AE2 no se venden en etapas tempranas.
- Reabastecimiento conservador con límite diario, existencias y/o condición de estación de trabajo; sin refresco ilimitado por romper y volver a colocar el puesto. La economía se prueba en mundo nuevo y en servidor dedicado.
- Usar aldeanos vanilla si basta; NPC propio solo si su comportamiento puede probarse, sincronizarse y mantenerse sin una dependencia oculta. No fingir “civiles vivos” con una entidad decorativa sin rutas/animación/comportamiento.

## Calidad de estructuras

- Catálogo, planos modulares, siluetas reconocibles a distancia, interiores transitables, rutas de entrada/salida, iluminación, narrativa ambiental y loot intencional.
- Evitar repeticiones por rotación de una caja simple: cada familia tendrá variantes y composición propias, límites de terreno y adaptación a bioma.
- No sustituir ni romper estructuras YUNG/vanilla. Colisiones, protección de bloques, spawns, rutas, loot y frecuencia son parte de pruebas.
- El soporte de Northstar y otras dimensiones exige confirmar sus IDs y reglas; no depender de nombres de dimensión adivinados.
- Se congelan y revisan todas las loot tables. Probar en mundos nuevos para descartar contenedores vacíos, tablas inválidas y loot desbalanceado.

## Interfaz y recursos visuales

El minimapa/atlas es el Mod 5; el paquete visual es aparte. El paquete de recursos podrá rediseñar menú principal, botones, pantallas de configuración y pantallas de entrada/lobby de Minecraft en una estética Create: brass/casing cálido, pergamino, pernos, palancas y engranajes. Debe poder desactivarse sin desinstalar los mods.

El usuario pidió no usar imágenes generadas por IA. Los recursos finales serán dibujados/diseñados manualmente como pixel art/vector original. Las referencias de arquitectura y textura servirán solo como estudio visual; se respetarán licencias y no se copiarán capturas, texturas ni las imágenes de stock con marcas de agua. Se separarán archivos fuente/editables del paquete compilado.

## Carpeta beta y respaldos

- Nombres deterministas por proyecto y versión MC, por ejemplo `modid-1.21.1-0.1.0-beta.1.jar`.
- Nunca presentar un archivo vacío, un JAR de ejemplo o una compilación sin probar como “listo para servidor”.
- Mantener un respaldo de la versión anterior por cada JAR. Al publicar una nueva beta: retirar solo el respaldo previo de ese mod, mover la beta vigente a su lugar de respaldo y colocar la nueva beta. No conservar una cadena de respaldos ilimitada.
- Los cinco proyectos solo entran en la carpeta de descargas cuando compilan, pasan pruebas de carga y tienen instrucciones/dependencias/lados documentados. La primera versión no tiene un respaldo anterior.

## Matriz mínima de salida antes de marcar una beta

1. Compilación reproducible en Java 21 / NeoForge 21.1.267 y `git diff --check`.
2. Inicio de cliente y servidor dedicado limpio con dependencias seleccionadas; inicio sin integraciones opcionales.
3. Conexión cliente-servidor con versiones iguales y rechazo/aviso claro ante versiones incompatibles.
4. Pruebas de registro/sincronización/render de interfaz; no cargar clases cliente en servidor.
5. Pruebas de mundo nuevo y chunks nuevos para estructuras/loot, si corresponde.
6. Perfiles básico y de estrés; revisar guardado, sincronización, distancia, frecuencia y uso de hilo principal.
7. Hash SHA-256, changelog, licencia, configuración, dependencias requeridas/opcionales y limitaciones publicados junto a cada beta.

## Estado de trabajo actual (20 de noviembre de 2026)

- La captura fija Minecraft 1.21.1, NeoForge 21.1.267 y LWJGL 3.3.3.
- Se creó la estructura Gradle de cinco subproyectos NeoForge independientes, sus entradas `@Mod`, metadata y un workflow de GitHub Actions para compilar con Java 21.
- **Cartografía, código parcial compilado en CI hasta `24e5e05`; nuevas claves de leyenda aún pendientes de build y prueba de juego:** muestrea solo chunks cargados, conserva historial local (50.000 celdas/dimensión), atlas M con zoom/paneo por flechas/recentrado R, minimapa N con cuatro esquinas F6 y tres tamaños F7, marcador de muerte ocultable/borrable y nombres de Waystones activados. La integración por reflexión aún necesita el JAR Waystones 21.1.47 exacto. Compilación no equivale a validar render/cliente.
- **Expediciones, estado parcial:** el build de `516ae70` falló por una referencia `flightComplete` huérfana; la corrección `f569f29` pasó CI (cinco proyectos). Después se refinó el tercer hito: distancia recta entre posiciones muestreadas por segundo durante planeo, descartando saltos >120 bloques y guardando por tramos de 250 para reducir I/O. El build de `c6238a3` falló: faltaba declarar el tipo auxiliar `FlightSample`; ya se añadió en el árbol de trabajo y la siguiente compilación queda pendiente. Sigue siendo una métrica aproximada. Se mantienen registro de biomas/dimensiones por UUID, `/expeditions`, configuración y recompensas opcionales, más registro inmediato de cambios de dimensión. Campañas completas, pruebas anti-exploit, persistencia/recompensas y Northstar siguen pendientes.
- El paquete visual separado tiene sprites originales de botón y sliders vanilla, arte de atlas/panel/engranaje, metadatos nine-slice y `pack.mcmeta` formato 34. La validación estructural/PNG pasó en CI; se añadieron motivos de engranaje a los sprites y requisito Pillow para regenerarlos. No se ha cargado en cliente; faltan menú principal y pantallas de lobby/configuración.
- `mods version 1/` no tiene JARs: se añadirán únicamente después de implementar y verificar cada mod.
- El archivo del Drive sí está localizado, pero `mods.zip` pesa 166 MB y el conector de Drive de esta sesión tiene límite de descarga de 100 MB. No he podido inspeccionar su interior; no se debe afirmar que sus dependencias o arranque estén verificados.
- El entorno de edición no tiene un JDK/Gradle instalados de forma local; el JRE auxiliar de Java 21 disponible no incluye `javac`. La CI remota ya compiló la base Cartografía y Expediciones con Java 21 / NeoForge 21.1.267. La compilación no sustituye inicio real en cliente/servidor ni pruebas del modpack.
