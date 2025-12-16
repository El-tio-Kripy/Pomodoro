# Pomodoro (Jetpack Compose)

Aplicación Android mínima de temporizador Pomodoro en Jetpack Compose. Incluye dos pantallas: Timer y Ajustes, con persistencia en DataStore.

## Pantallas

- **Timer**: muestra fase actual (TRABAJO / DESCANSO), tiempo restante mm:ss y botones Start, Stop y Reset. Muestra el estado de Auto-start.
- **Ajustes**: permite configurar minutos de Trabajo y Descanso, y activar/desactivar Auto-start. Los cambios se guardan en DataStore.

## Valores predeterminados

- Trabajo: 25 minutos  
- Descanso: 5 minutos  
- Auto-start: activado

## Cómo abrir el proyecto

1. Instala [Android Studio](https://developer.android.com/studio).
2. Abre el directorio del repositorio (`Open > Existing Project`).
3. Sincroniza Gradle cuando se solicite (proyecto en Kotlin DSL: `build.gradle.kts`, `settings.gradle.kts`).

## Cómo ejecutar

- **Desde Android Studio**: selecciona un dispositivo/emulador y pulsa **Run**.
- **Desde línea de comandos**:
  ```bash
  ./gradlew assembleDebug
  ```
  El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Flujo del temporizador

1. Start inicia TRABAJO.
2. Al llegar a `00:00`, cambia a DESCANSO.
3. Al terminar DESCANSO, vuelve a TRABAJO.
4. Este ciclo se repite hasta pulsar Stop (reinicia a TRABAJO con el tiempo completo).  
5. Reset restablece el tiempo de la fase actual sin cambiar de fase.  
6. Con Auto-start activado, las fases comienzan automáticamente; si está desactivado, cada cambio de fase queda pausado hasta pulsar Start.

## Arquitectura breve

- **TimerEngine**: temporizador basado en `SystemClock.elapsedRealtime()` para minimizar drift y manejar transiciones de fase.
- **TimerState**: fase, tiempo restante, estado de ejecución y ajustes vigentes.
- **SettingsRepository**: persiste ajustes en DataStore Preferences.
- **MainActivity + Jetpack Compose Navigation**: navega entre Timer y Ajustes en modo portrait.
