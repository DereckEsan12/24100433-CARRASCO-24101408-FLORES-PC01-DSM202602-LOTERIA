package dev.dereck.pc1moviles.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dereck.pc1moviles.data.SorteoEstado
import dev.dereck.pc1moviles.jugada.JugadaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JugadaScreen(
    email: String,
    uid: String,
    viewModel: JugadaViewModel,
    onLogout: () -> Unit,
    onAvanzarSeguimiento: () -> Unit
) {
    val sorteo by viewModel.sorteo.collectAsState()
    val seleccionados by viewModel.seleccionados.collectAsState()
    val jugadaGuardada by viewModel.jugadaGuardada.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val sorteoExiste by viewModel.sorteoExiste.collectAsState()

    var nombreSorteo by remember { mutableStateOf("Sorteo de prueba") }
    var fechaSorteo by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        viewModel.escucharSorteo()
        viewModel.cargarJugada(uid)
    }

    val abierto = sorteo?.estado == SorteoEstado.ABIERTO

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sesión: $email", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) { Text("Salir") }
        }
        Spacer(Modifier.height(8.dp))
        Text("Registro de jugada", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            CircularProgressIndicator()
            Text("Cargando sorteo...")
        } else if (!sorteoExiste || sorteo == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("No hay sorteo de prueba en Firestore.", style = MaterialTheme.typography.titleSmall)
                    Text("Colección: sorteos / documento: sorteo_prueba", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nombreSorteo, onValueChange = { nombreSorteo = it }, label = { Text("Nombre del sorteo") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = fechaSorteo, onValueChange = { fechaSorteo = it }, label = { Text("Fecha (ej. 2026-02-10)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.crearSorteoPrueba(nombreSorteo, fechaSorteo) }, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isSaving) "Creando..." else "Crear sorteo abierto")
                    }
                }
            }
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sorteo: ${sorteo!!.nombre}", style = MaterialTheme.typography.titleMedium)
                    Text("Fecha: ${sorteo!!.fecha.ifBlank { "—" }}")
                    Text("Estado: ${sorteo!!.estado}")
                    if (!abierto) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "El sorteo ya inició. No se puede registrar ni modificar jugadas.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(Modifier.height(4.dp))
                        Text("Sorteo abierto: puedes registrar tu jugada.")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Elige 6 números distintos (1–45): ${seleccionados.size}/6")
            if (jugadaGuardada != null) {
                Text("Tu jugada guardada: ${jugadaGuardada!!.numeros.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (n in 1..45) {
                    FilterChip(
                        selected = n in seleccionados,
                        onClick = { viewModel.toggleNumero(n) },
                        enabled = abierto && !isSaving,
                        label = { Text(n.toString()) }
                    )
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            if (mensaje != null) {
                Spacer(Modifier.height(8.dp))
                Text(mensaje!!, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.limpiarSeleccion() }, enabled = abierto && !isSaving, modifier = Modifier.weight(1f)) {
                    Text("Limpiar")
                }
                Button(
                    onClick = { viewModel.guardarJugada(uid) },
                    enabled = abierto && !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Guardando..." else "Guardar jugada")
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAvanzarSeguimiento, modifier = Modifier.fillMaxWidth()) {
                Text("Avanzar al seguimiento")
            }
        }
    }
}

@Composable
fun SeguimientoScreen(email: String, uid: String, viewModel: dev.dereck.pc1moviles.seguimiento.SeguimientoViewModel, onVolver: () -> Unit, onLogout: () -> Unit) {
    val bolillas by viewModel.bolillas.collectAsState()
    val jugada by viewModel.jugada.collectAsState()
    LaunchedEffect(uid) { viewModel.escuchar(uid) }
    val aciertos = jugada.intersect(bolillas.map { it.numero }.toSet()).size
    val resultado = when {
        bolillas.size < 6 -> "Sorteo en curso ${bolillas.size}/6 - Aciertos: $aciertos"
        aciertos == 6 -> "GANADOR"
        else -> "SIGUE INTENTANDO"
    }
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Seguimiento del sorteo", style = MaterialTheme.typography.headlineSmall)
        Text("Sesión: $email")
        Spacer(Modifier.height(12.dp))
        Text("Bolillas: ${if (bolillas.isEmpty()) "Esperando..." else bolillas.map { it.numero }.joinToString(" - ")}")
        Text("Tu jugada: ${if (jugada.isEmpty()) "—" else jugada.joinToString(", ")}")
        Text("Aciertos: $aciertos")
        Text(resultado, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        TextButton(onClick = onLogout) { Text("Cerrar sesión") }
    }
}
