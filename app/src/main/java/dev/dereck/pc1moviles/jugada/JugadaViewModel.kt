package dev.dereck.pc1moviles.jugada

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dev.dereck.pc1moviles.data.Jugada
import dev.dereck.pc1moviles.data.SORTEO_ID
import dev.dereck.pc1moviles.data.Sorteo
import dev.dereck.pc1moviles.data.SorteoEstado
import dev.dereck.pc1moviles.data.validarJugada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JugadaViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _sorteo = MutableStateFlow<Sorteo?>(null)
    val sorteo: StateFlow<Sorteo?> = _sorteo

    private val _seleccionados = MutableStateFlow<Set<Int>>(emptySet())
    val seleccionados: StateFlow<Set<Int>> = _seleccionados

    private val _jugadaGuardada = MutableStateFlow<Jugada?>(null)
    val jugadaGuardada: StateFlow<Jugada?> = _jugadaGuardada

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _sorteoExiste = MutableStateFlow(true)
    val sorteoExiste: StateFlow<Boolean> = _sorteoExiste

    private var sorteoListener: ListenerRegistration? = null

    fun clearMessages() {
        _error.value = null
        _mensaje.value = null
    }

    /** Escucha la cabecera del sorteo en tiempo real. */
    fun escucharSorteo() {
        if (sorteoListener != null) return
        _isLoading.value = true
        sorteoListener = db.collection("sorteos").document(SORTEO_ID)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "No se pudo cargar el sorteo: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    _sorteoExiste.value = false
                    _sorteo.value = null
                    return@addSnapshotListener
                }
                _sorteoExiste.value = true
                _sorteo.value = Sorteo(
                    id = snapshot.id,
                    nombre = snapshot.getString("nombre") ?: "Sorteo de prueba",
                    fecha = snapshot.getString("fecha") ?: "",
                    estado = snapshot.getString("estado") ?: SorteoEstado.ABIERTO
                )
            }
    }

    /** Carga la jugada del usuario actual (solo la de su sesión). */
    fun cargarJugada(uid: String) {
        db.collection("sorteos").document(SORTEO_ID)
            .collection("jugadas").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nums = (doc.get("numeros") as? List<*>)?.mapNotNull {
                    (it as? Number)?.toInt()
                } ?: emptyList()
                if (doc.exists() && nums.isNotEmpty()) {
                    _jugadaGuardada.value = Jugada(SORTEO_ID, uid, nums.sorted())
                    _seleccionados.value = nums.toSet()
                } else {
                    _jugadaGuardada.value = null
                }
            }
            .addOnFailureListener { e ->
                _error.value = "No se pudo cargar tu jugada: ${e.message}"
            }
    }

    /** Crea el sorteo de prueba en estado abierto (para no depender de la consola). */
    fun crearSorteoPrueba(nombre: String = "Sorteo de prueba", fecha: String = "") {
        _isSaving.value = true
        val data = mapOf(
            "nombre" to nombre.ifBlank { "Sorteo de prueba" },
            "fecha" to fecha,
            "estado" to SorteoEstado.ABIERTO
        )
        db.collection("sorteos").document(SORTEO_ID)
            .set(data)
            .addOnSuccessListener {
                _isSaving.value = false
                _mensaje.value = "Sorteo creado y abierto."
            }
            .addOnFailureListener { e ->
                _isSaving.value = false
                _error.value = "No se pudo crear el sorteo: ${e.message}"
            }
    }

    fun toggleNumero(n: Int) {
        val actual = _seleccionados.value.toMutableSet()
        if (n in actual) actual.remove(n)
        else {
            if (actual.size >= 6) {
                _error.value = "Solo puedes elegir 6 números. Quita uno para cambiarlo."
                return
            }
            actual.add(n)
        }
        _seleccionados.value = actual
        _error.value = null
    }

    fun limpiarSeleccion() {
        _seleccionados.value = emptySet()
    }

    /** Guarda la jugada asociada al usuario y al sorteo. Solo si está abierto. */
    fun guardarJugada(uid: String) {
        clearMessages()
        val estado = _sorteo.value?.estado
        if (_sorteo.value == null && !_sorteoExiste.value) {
            _error.value = "Primero crea el sorteo de prueba."
            return
        }
        if (estado != null && estado != SorteoEstado.ABIERTO) {
            _error.value = "El sorteo ya inició ($estado). No se puede registrar ni modificar jugadas."
            return
        }
        val numeros = _seleccionados.value.toList()
        validarJugada(numeros)?.let {
            _error.value = it
            return
        }
        _isSaving.value = true
        val data = mapOf(
            "sorteoId" to SORTEO_ID,
            "uid" to uid,
            "numeros" to numeros.sorted(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("sorteos").document(SORTEO_ID)
            .collection("jugadas").document(uid)
            .set(data)
            .addOnSuccessListener {
                _isSaving.value = false
                _jugadaGuardada.value = Jugada(SORTEO_ID, uid, numeros.sorted())
                _mensaje.value = "Jugada guardada: ${numeros.sorted().joinToString(", ")}"
            }
            .addOnFailureListener { e ->
                _isSaving.value = false
                _error.value = "No se pudo guardar la jugada: ${e.message}"
            }
    }

    override fun onCleared() {
        sorteoListener?.remove()
        sorteoListener = null
    }
}
