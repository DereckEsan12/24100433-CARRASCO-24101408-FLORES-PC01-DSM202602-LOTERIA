package dev.dereck.pc1moviles.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _user.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(listener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(listener)
    }

    fun clearError() {
        _error.value = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!validarCredenciales(email, password)) return
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _user.value = it.user
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = mensajeAmigable(e.message)
            }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        if (!validarCredenciales(email, password)) return
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _user.value = it.user
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = mensajeAmigable(e.message)
            }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }

    private fun validarCredenciales(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Ingresa correo y contraseña."
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _error.value = "Ingresa un correo válido."
            return false
        }
        if (password.length < 6) {
            _error.value = "La contraseña debe tener al menos 6 caracteres."
            return false
        }
        return true
    }

    private fun mensajeAmigable(detalle: String?): String {
        if (detalle == null) return "Ocurrió un error de autenticación."
        val d = detalle.lowercase()
        return when {
            "invalid-credential" in d || "wrong-password" in d ->
                "Credenciales incorrectas. Verifica correo y contraseña."
            "user-not-found" in d -> "No existe una cuenta con ese correo."
            "email-already-in-use" in d || "email already in use" in d ->
                "Ese correo ya está registrado. Inicia sesión."
            "network" in d -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            else -> "Error de autenticación: $detalle"
        }
    }
}
