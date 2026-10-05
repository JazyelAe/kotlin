AquaTrack - Aplicación nativa Android (Kotlin + Jetpack Compose)

OPCIÓN A (la más segura): crear el proyecto desde Android Studio
1. File > New > New Project > "Empty Activity" (la que usa Jetpack Compose).
2. Name: AquaTrack   Package name: com.jaziel.aquatrack   Language: Kotlin   Minimum SDK: API 26.
3. Abre app/src/main/java/com/jaziel/aquatrack/MainActivity.kt y reemplaza TODO su contenido
   por el del MainActivity.kt de este proyecto.
4. Espera el Gradle Sync y pulsa Run (▶) con un emulador (por ejemplo Pixel 6, API 34).

OPCIÓN B: abrir esta carpeta con File > Open. Si Android Studio lo pide, acepta crear el Gradle
wrapper / usar la versión de Gradle que sugiera, y haz Sync.

Funciones: sumar 250 ml o 500 ml, indicador circular con porcentaje, registro de tomas con hora,
mensaje al llegar a 2000 ml, reiniciar día. Los datos se guardan con SharedPreferences.
