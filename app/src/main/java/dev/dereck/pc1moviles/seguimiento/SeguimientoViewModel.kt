package dev.dereck.pc1moviles.seguimiento

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dev.dereck.pc1moviles.data.SORTEO_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Bolilla(val numero: Int = 0, val orden: Int = 0)

class SeguimientoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var reg1: ListenerRegistration? = null
    private var reg2: ListenerRegistration? = null

    private val _bolillas = MutableStateFlow<List<Bolilla>>(emptyList())
    val bolillas: StateFlow<List<Bolilla>> = _bolillas
    private val _jugada = MutableStateFlow<List<Int>>(emptyList())
    val jugada: StateFlow<List<Int>> = _jugada
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun escuchar(uid: String) {
        if (reg1 != null) return
        reg1 = db.collection("sorteos").document(SORTEO_ID)
            .collection("bolillas").orderBy("orden", Query.Direction.ASCENDING)
            .addSnapshotListener { s, e ->
                if (e != null) { _error.value = e.message; return@addSnapshotListener }
                _bolillas.value = s?.documents?.map {
                    Bolilla((it.getLong("numero") ?: 0).toInt(), (it.getLong("orden") ?: 0).toInt())
                } ?: emptyList()
            }
        reg2 = db.collection("sorteos").document(SORTEO_ID)
            .collection("jugadas").document(uid)
            .addSnapshotListener { d, e ->
                if (e != null) return@addSnapshotListener
                _jugada.value = (d?.get("numeros") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
            }
    }
    override fun onCleared() { reg1?.remove(); reg2?.remove() }
}
