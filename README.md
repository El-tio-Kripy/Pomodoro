# Pomodoro (Jetpack Compose)

Una app Android de temporizador Pomodoro, simple y lista para evaluar. Incluye un flujo automático de trabajo/descanso, ajustes persistentes en `SharedPreferences` y una UI minimalista en Jetpack Compose.

## Pantallas

- **Timer**: muestra la fase actual (Trabajo, Descanso corto o Descanso largo), el tiempo restante en MM:SS, el indicador de ciclo (`Pomodoro x / N`) y botones de Iniciar/Pausa, Detener/Reset y Saltar.
- **Ajustes**: edita las duraciones en minutos y cada cuántos pomodoros ocurre el descanso largo. Los valores se guardan inmediatamente y se aplican al volver al timer.

## Valores predeterminados

- Trabajo: 25 minutos  
- Descanso corto: 5 minutos  
- Descanso largo: 15 minutos  
- Descanso largo cada: 4 pomodoros

## Cómo abrir el proyecto

1. Instala [Android Studio](https://developer.android.com/studio).
2. Abre el directorio del repositorio en Android Studio (`Open > Existing Project`).
3. Sincroniza Gradle cuando se solicite.

## Cómo ejecutar

- **Desde Android Studio**: selecciona un dispositivo/emulador y pulsa **Run**.
- **Desde línea de comandos**:
  ```bash
  ./gradlew assembleDebug
  ```
  El APK quedará en `app/build/outputs/apk/debug/app-debug.apk`.

## Flujo del temporizador

1. Al abrir: se muestra `25:00` en la fase TRABAJO.
2. **Iniciar**: comienza la cuenta regresiva. **Pausa** detiene el conteo.  
3. **Saltar**: avanza inmediatamente a la siguiente fase.  
4. Al llegar a `00:00`, pasa automáticamente a descanso corto o largo según el ciclo configurado y, al terminar, vuelve a TRABAJO. Esto se repite hasta que el usuario pulse **Detener**, que reinicia el ciclo en TRABAJO con el tiempo completo configurado.

## Arquitectura breve

- **TimerEngine**: controla el conteo basado en `SystemClock.elapsedRealtime()` para evitar drift y maneja transiciones automáticas entre fases.
- **TimerState**: representa fase, segundos restantes, estado de ejecución y progreso del ciclo.
- **SettingsRepository**: persiste y lee ajustes con `SharedPreferences`.
- **MainActivity / SettingsActivity**: dos pantallas Compose en modo portrait.
