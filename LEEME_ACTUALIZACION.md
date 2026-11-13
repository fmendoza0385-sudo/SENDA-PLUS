# SENDA PLUS — actualización de lista M3U8 única

## Contenido
Este paquete es un **paquete de actualización (overlay)** para aplicar sobre la raíz del repositorio:
`https://github.com/fmendoza0385-sudo/SENDA-PLUS`

Incluye:
- `app/src/main/java/com/sendaplus/iptv/M3uManager.kt`
- `app/src/main/java/com/sendaplus/iptv/PlaylistStore.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/assets/data_center.m3u8`

## Cambios
1. Integra los 84 canales extraídos del archivo `data center.m3u8`.
2. Numeración consecutiva desde 100 hasta 183.
3. Elimina la restauración de listas importadas anteriormente.
4. Oculta los controles de importar M3U y cambiar listas.
5. Conserva la guía, la reproducción a pantalla completa y la navegación CH+/CH−.
6. Conserva el último índice de canal seleccionado.
7. La lista original se incluye como `assets/data_center.m3u8` y las URLs también quedan integradas en `M3uManager.kt` para que la aplicación cargue los canales al iniciar.

## Aplicar en GitHub
1. Descarga el ZIP y extráelo.
2. Copia la carpeta `app` incluida dentro de la raíz del repositorio SENDA-PLUS.
3. Acepta reemplazar los archivos con la misma ruta.
4. Sube los cambios a la rama `main`.
5. En GitHub abre **Actions → Compilar APK SENDA PLUS**.
6. Descarga el artefacto `SENDA-PLUS-APK` cuando el workflow termine correctamente.

## Compilación
El paquete no contiene el SDK de Android ni un APK compilado. La compilación se realiza en GitHub Actions con el workflow que ya existe en el repositorio (JDK 17 / Gradle 8.13).

## Importante
- Este ZIP no modifica directamente el repositorio ni publica commits.
- Se conserva el motor Media3 con fallback LibVLC que ya tiene el proyecto.
- La reproducción depende de que las URLs del proveedor sigan activas y sean accesibles desde el dispositivo.
- El archivo M3U8 contiene direcciones de streaming suministradas por el usuario; comprueba que tienes autorización para utilizarlas.
- Para instalar la nueva APK como actualización sobre una anterior, debe conservar el mismo `applicationId` y la misma firma de publicación.
