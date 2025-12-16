# Pomodoro

Aplicación Android sencilla de Pomodoro desarrollada con Jetpack Compose y arquitectura MVVM. El temporizador ejecuta ciclos automáticos de trabajo, descanso corto y descanso largo, guardando el historial de fases en Firebase Cloud Firestore usando autenticación anónima.

## Requisitos
- Android Studio Hedgehog o posterior.
- JDK 17.
- Dispositivo o emulador con Android 8.0 (API 26) o superior.
- Gradle 8.9 instalado de forma local (solo si necesitas regenerar el wrapper).

## Configuración de Firebase
1. Crea un proyecto en Firebase Console.
2. Añade una app Android con el **package name** `com.example.pomodoro`.
3. Descarga el archivo `google-services.json` desde la consola de Firebase y colócalo en `app/`.
4. En Firebase Console, habilita **Authentication → Sign-in method → Anonymous**.
5. En Firebase Console, habilita **Cloud Firestore** en modo de producción o prueba según tus necesidades.

> Si `google-services.json` no está presente, la compilación puede fallar al ejecutar el plugin de Google Services. Añade el archivo en `app/` y vuelve a compilar.

## Compilación
```bash
# Si necesitas regenerar el wrapper (por ausencia de gradle-wrapper.jar):
gradle wrapper --gradle-version 8.9 --distribution-type bin

./gradlew assembleDebug
```

## Funcionalidad
- Ciclo automático: Trabajo (25 min) → Descanso corto (5 min) → Trabajo … con descanso largo (15 min) cada 4 pomodoros.
- Controles: **Iniciar**, **Detener**, **Reset**.
- Vista única con Compose sin navegación ni layouts XML.
- Historial en Firestore con campos: `timestamp`, `phase`, `durationSeconds`, `pomodoroIndex`, `deviceId`, `sessionId`.
