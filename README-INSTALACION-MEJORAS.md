# SENDA PLUS — paquete de mejoras

Este ZIP contiene archivos para reemplazar/agregar en la raíz del repositorio `fmendoza0385-sudo/SENDA-PLUS`.

## Archivos incluidos

- `app/src/main/java/com/sendaplus/iptv/MainActivity.kt`
- `app/src/main/java/com/sendaplus/iptv/M3uManager.kt`
- `app/src/main/java/com/sendaplus/iptv/PlaylistStore.kt` (nuevo)
- `app/src/main/res/layout/activity_main.xml`
- Este documento.

## Cambios

1. La guía aparece desde el lado izquierdo y permanece oculta durante la reproducción.
2. OK/ENTER o tocar el video abre/cierra la guía.
3. El OSD del canal se muestra al cambiar y se oculta automáticamente a los 3 segundos.
4. Las listas importadas y sus canales se guardan en SharedPreferences como JSON.
5. Se agrega el menú **MIS LISTAS** para cambiar entre listas guardadas y canales predeterminados.
6. Media3 intenta reproducir primero. Si no reporta ningún primer fotograma en 10 segundos, se intenta LibVLC. Esto cubre casos donde el audio continúa pero el video no aparece.

## Cómo instalar los cambios

1. Descarga una copia/ZIP del repositorio actual como respaldo.
2. Extrae este paquete en la raíz del proyecto, conservando las rutas de carpetas.
3. Acepta reemplazar `MainActivity.kt`, `M3uManager.kt` y `activity_main.xml`.
4. Agrega el nuevo archivo `PlaylistStore.kt`.
5. Sube los cambios a GitHub y ejecuta el workflow **Compilar APK SENDA PLUS**.

## Importante sobre la compatibilidad de video

El cambio automático a LibVLC se activa cuando Media3 informa un error o no entrega un primer fotograma en 10 segundos. Es una estrategia de respaldo, no una garantía de que toda URL/códec/servidor sea reproducible. Los canales pueden usar perfiles, niveles, contenedores, cifrado o encabezados HTTP diferentes. Se debe probar la APK resultante en el dispositivo real con los canales RCN, Caracol y Canal 1.

## Persistencia de listas

Las listas se guardan como datos locales de la aplicación. Al desinstalar la APK o borrar sus datos desde Android, se eliminan también las listas guardadas. El menú **MIS LISTAS** permite seleccionar las listas importadas anteriormente.
