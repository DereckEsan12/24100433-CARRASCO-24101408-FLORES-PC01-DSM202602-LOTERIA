# PC01 DSM202602 Lotería

App Android (Kotlin + Jetpack Compose + Firebase Auth + Firestore) — Práctica Calificada 01.

## Integrantes
- Carrasco — 24100433 — 24100433@ue.edu.pe
- Flores — 24101408

## Alcance implementado (parte 1: Autenticación y registro de la jugada)
- [x] Crear cuenta con correo/contraseña e iniciar sesión (Firebase Authentication).
- [x] Muestra correo del usuario autenticado y mensajes de error.
- [x] Registro de jugada: exactamente 6 números distintos entre 1 y 45.
- [x] Jugada guardada asociada al usuario (`uid`) y al sorteo (`sorteo_prueba`).
- [x] Solo muestra la jugada del usuario en sesión.
- [x] Bloquea registro/modificación si el sorteo no está `abierto`.
- [x] Navegación: Login ↔ Registro → Jugada → Seguimiento (placeholder), con volver/avanzar y logout. Nadie queda atrapado.

La pantalla de **Seguimiento en tiempo real** (bolillas, aciertos, GANADOR / SIGUE INTENTANDO) es la siguiente parte y está como placeholder navegable.

## Estructura Firestore
```
sorteos/{sorteoId}                  # ej. sorteos/sorteo_prueba
  nombre: "Sorteo de prueba"
  fecha: "2026-02-10"
  estado: "abierto" | "en_curso" | "finalizado"

sorteos/{sorteoId}/jugadas/{uid}
  sorteoId, uid, numeros: [5,12,18,23,34,41], updatedAt

sorteos/{sorteoId}/bolillas/{orden}  # (parte 2)
  sorteoId, numero, orden 1..6
```
ID de sorteo usado en código: `SORTEO_ID = "sorteo_prueba"` (`data/LoteriaModels.kt`).

## Configuración Firebase (obligatorio para correr)
1. En [Firebase Console](https://console.firebase.google.com/) crea/importa el proyecto.
2. Agrega app Android con `applicationId = dev.dereck.pc1moviles`.
3. Descarga `google-services.json` y colócalo en `app/google-services.json` (no viene en el repo).
4. Activa **Authentication → Email/Password** y crea **Firestore Database**.
5. Reglas mínimas de prueba (ajustar según tu clase):
```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /sorteos/{sorteoId} { allow read: if request.auth != null; allow write: if request.auth != null; }
    match /sorteos/{sorteoId}/{sub=**} { allow read: if request.auth != null; allow write: if request.auth != null; }
  }
}
```
6. Crea el sorteo desde la app (botón "Crear sorteo abierto") o desde consola:
   `sorteos/sorteo_prueba = { nombre, fecha, estado: "abierto" }`.
7. Compila con Android Studio (JBR 17+) o:
```
./gradlew :app:assembleDebug
```

> Sin `app/google-services.json` el build falla en `:app:processDebugGoogleServices` — es esperado.

## Demo sugerida (2 usuarios, mismo sorteo)
- Usuario A: `5, 12, 18, 23, 34, 41` → GANADOR
- Usuario B: `5, 12, 18, 23, 34, 42` → SIGUE INTENTANDO

## Archivos clave
- `MainActivity.kt` + `navigation/AppNav.kt`
- `auth/AuthViewModel.kt` — login/registro/logout
- `jugada/JugadaViewModel.kt` — escucha sorteo, valida y guarda jugada
- `ui/screens/AuthScreens.kt` — Login / Registro
- `ui/screens/JugadaScreens.kt` — Jugada + Seguimiento (placeholder)
