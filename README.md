# SENDA PLUS 2.0

Proyecto Android IPTV con reproductor a pantalla completa, guía superpuesta
y estrategia de doble motor: Media3 como motor principal y LibVLC como
alternativa cuando Media3 informa un error de reproducción.

## Comportamiento de la guía
- Smartphone: tocar el video muestra/oculta la guía.
- Android TV / TV Box: OK/ENTER abre/cierra la guía.
- BACK cierra la guía.
- CH+ / CH- cambia de canal cuando la guía está cerrada.
- Al elegir un canal, la guía se oculta y el video continúa a pantalla completa.

## Motores de reproducción
- Media3 intenta reproducir primero.
- Si Media3 informa PlaybackException, la aplicación intenta reproducir
  el canal con LibVLC.
- El fallback no garantiza compatibilidad con todos los flujos: depende de
  la URL, cabeceras, códecs, DRM, red y arquitectura del dispositivo.

## Compilar
GitHub Actions está configurado en `.github/workflows/main.yml`.
Utiliza JDK 17 y Gradle 8.9. El APK Debug se publica como Artifact.

## Nota
Las URLs de canales de ejemplo incluidas en M3uManager.kt deben ser
reemplazadas por fuentes legítimas y autorizadas. Para diagnosticar un
canal negro se requiere conocer su URL autorizada y los códecs reales.
