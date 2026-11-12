# SENDA PLUS Android

Proyecto Android IPTV en Kotlin preparado para compilar con GitHub Actions.

## Subida correcta a GitHub

Descomprime este ZIP y sube TODO su contenido al nivel principal del repositorio.
La estructura debe mostrar en la raiz:
- `.github/workflows/main.yml`
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/`

No subas solamente `main.yml` ni guardes el proyecto completo dentro de otra carpeta comprimida.

## Compilacion

El workflow usa JDK 17, Gradle 8.9, Ubuntu 24.04 y publica el APK Debug como Artifact.
