# Brass Cartography — estado de desarrollo

Mod independiente para Minecraft 1.21.1 / NeoForge 21.1.267. La base y el muestreo de mapas han compilado en CI; las funciones visuales deben probarse en cliente y la compatibilidad Waystones en una instancia con la versión exacta del pack antes de publicar un JAR.

## Controles actuales

- **M:** abrir/cerrar atlas de campo. Las flechas mueven el centro del atlas; **R** vuelve al jugador. Los botones `−`/`+` cambian el zoom.
- **N:** mostrar u ocultar minimapa.
- **F6:** mover el minimapa entre las cuatro esquinas.
- **F7:** recorrer tres tamaños.
- En el atlas se puede ocultar el marcador de muerte, borrarlo por completo y activar/desactivar el minimapa.

Las teclas se pueden reasignar desde Controles de Minecraft. La posición y el tamaño del minimapa, su visibilidad y el estado del marcador se guardan en `config/brass_cartography.properties`.

## Datos y límites

- El cliente solo lee terreno de chunks ya cargados; no fuerza cargas ni generación de chunks.
- El historial local se guarda por dimensión en `config/brass_cartography/maps/`, con límite de 50.000 celdas por dimensión.
- El marcador de muerte es local al cliente.
- Si Waystones está presente, la integración por reflexión lee únicamente los destinos activados del jugador local y de la misma dimensión, sin cargar chunks. La compatibilidad con el JAR Waystones 21.1.47 y los nombres de destino siguen pendientes de prueba en juego.

No está todavía aprobado como modpack-ready: falta probar carga en cliente/servidor, rendimiento, renderización en escalas/resoluciones distintas, reinicio y la integración opcional real.
