# AutoSkip (Android)

Pulsa automáticamente el botón **«Saltar anuncio» / «Skip ad»** en la app de YouTube mediante un `AccessibilityService`.

[![Descargar APK](https://img.shields.io/github/v/release/Abelgs716/SkipAnuncios?label=Descargar%20APK)](https://github.com/Abelgs716/SkipAnuncios/releases/latest)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Apoya%20el%20proyecto-FF5E5B?logo=ko-fi&logoColor=white)](https://ko-fi.com/abelgs716)
[![Licencia MIT](https://img.shields.io/badge/licencia-MIT-blue)](LICENSE)

## Descargar e instalar en el móvil

1. Descarga `AutoSkip-1.1.apk` desde [Releases](https://github.com/Abelgs716/SkipAnuncios/releases/latest) en el propio móvil.
2. Ábrelo y permite *Instalar apps desconocidas* para el navegador o el gestor de archivos cuando Android lo pida.
3. Abre AutoSkip y sigue la pantalla de permisos: *Ajustes → Accesibilidad → AutoSkip → Activar*.
   - Si aparece **«Ajuste restringido»** (Android 13 o superior): *Ajustes → Apps → AutoSkip → ⋮ → Permitir ajustes restringidos* y vuelve a intentarlo.
4. El estado debe mostrar 🟢 **Activo**. Abre YouTube y AutoSkip pulsará «Saltar anuncio» en cuanto aparezca.

Requisitos: Android 8.0 o superior y la app oficial de YouTube.

### Instalar desde un PC por USB

Con la depuración USB activada en el móvil y [platform-tools](https://developer.android.com/tools/releases/platform-tools) instalado en `%USERPROFILE%\AndroidDev\sdk`, ejecuta `instalar-en-movil.bat`. Instala `AutoSkip-1.1.apk` y activa el servicio de Accesibilidad sin pasar por Ajustes.

## Compilar

Necesitas JDK 17 y el SDK de Android (o [Android Studio](https://developer.android.com/studio), que incluye ambos). Abre la carpeta con *File → Open* o usa la terminal:

```
gradlew test            # tests unitarios de la detección de texto
gradlew assembleDebug   # APK de pruebas
gradlew assembleRelease # APK firmado (necesita keystore.properties)
```

Para firmar la versión de publicación, crea `keystore.properties` en la raíz (está en `.gitignore`, nunca se sube):

```
storeFile=C:/ruta/a/autoskip-release.jks
storePassword=...
keyAlias=autoskip
keyPassword=...
```

Todas las actualizaciones deben firmarse con **la misma clave**; si se pierde, los usuarios tendrán que desinstalar para instalar una versión nueva.

## Cómo funciona

| Archivo | Función |
|---|---|
| `AutoSkipService.kt` | Servicio en segundo plano. Solo recibe eventos de `com.google.android.youtube`, agrupa los eventos (como mucho un escaneo cada 250 ms) y espera 1,5 s de pausa tras cada clic. |
| `SkipAdFinder.kt` | Busca el botón: primero por ID de vista (`skip_ad_button`, …; no depende del idioma) y después por texto o descripción que coincida exactamente con una frase de «saltar anuncio». |
| `SkipAdMatcher.kt` | Frases de «saltar anuncio» en más de 30 idiomas. Normaliza el texto (mayúsculas, tildes, signos). |
| `MainActivity.kt` | Estado (🟢 / 🔴 / ⚠️), interruptor Activar/Desactivar y contador. |
| `PermissionActivity.kt` | Explicación del permiso y accesos directos a los ajustes. |

Medidas de seguridad para no tocar nada más:
- Las palabras sueltas («Skip», «Saltar») no cuentan; solo frases completas o el ID del botón.
- Etiquetas de más de 40 caracteres descartadas; el nodo debe ser visible y estar habilitado.
- Si el elemento pulsable es mayor del 25 % de la pantalla (p. ej., el reproductor), **no** se pulsa.
- Sin permiso `INTERNET`: la app no puede enviar datos.

Funciona igual en vertical y en horizontal (usa coordenadas absolutas y el área total de la pantalla).

## Pruebas realizadas (26/09/2026, emulador Pixel 7, Android 15, YouTube 21.38.130)

| Prueba | Resultado |
|---|---|
| Vertical, YouTube en inglés | ✅ 2 anuncios saltados |
| Horizontal (pantalla completa), inglés | ✅ 2 anuncios saltados, el vídeo sigue reproduciéndose |
| Vertical, YouTube en español | ✅ Saltado; botón «Saltar» / etiqueta «Saltar anuncio» |
| Solo detección por texto (sin IDs) | ✅ Saltado a partir de «Saltar anuncio» |
| Interruptor desactivado | ✅ 0 pulsaciones con el botón visible |
| Anuncios no saltables y banners | ✅ No los toca |
| Reinstalar o actualizar la app | ✅ El servicio sigue activo |
| APK de publicación firmado (R8) | ✅ Salta igual que la versión de pruebas |

Todas las pulsaciones registradas fueron sobre `com.google.android.youtube:id/skip_ad_button` (logcat, etiqueta `AutoSkip`). Pendiente: probar en un móvil físico.

## Mantenimiento

YouTube cambia su interfaz con frecuencia. Si deja de funcionar, activa *Opciones de desarrollador → Mostrar límites de diseño* o usa *Layout Inspector* / `uiautomator dump` con un anuncio en pantalla para ver el nuevo ID o texto del botón y añádelo a `SKIP_VIEW_IDS` o `RAW_PHRASES` en `SkipAdMatcher.kt`.

## Aviso sobre Google Play

La política de Google Play prohíbe las apps que bloquean o interfieren con los anuncios de otras apps, y revisa con lupa el uso de `AccessibilityService`. Saltar anuncios también puede incumplir los Términos de YouTube. Por eso AutoSkip se distribuye solo como APK en GitHub Releases, no en Play Store.

## ¿Y iPhone?

No es posible en iOS. Apple no ofrece ninguna API equivalente a `AccessibilityService`: cada app vive aislada en su sandbox y no puede leer ni pulsar la interfaz de otra. Tampoco Atajos, Control por voz o Control por botón pueden automatizar esto desde una app de terceros, y App Store no aceptaría una app así. La única forma oficial de ver YouTube sin anuncios en iPhone es YouTube Premium.

## ☕ Apoya el proyecto

AutoSkip es gratuito, sin anuncios y de código abierto. Si te ahorra tiempo, puedes invitarme a un café en **[Ko-fi](https://ko-fi.com/abelgs716)**. Las donaciones ayudan a mantenerlo al día cada vez que YouTube cambia su interfaz.

También ayuda mucho darle una ⭐ al repositorio y compartirlo.

## Licencia

[MIT](LICENSE). AutoSkip no está afiliado a YouTube ni a Google.
