package dev.dereck.pc1moviles.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.dereck.pc1moviles.auth.AuthViewModel
import dev.dereck.pc1moviles.jugada.JugadaViewModel
import dev.dereck.pc1moviles.ui.screens.JugadaScreen
import dev.dereck.pc1moviles.ui.screens.LoginScreen
import dev.dereck.pc1moviles.ui.screens.RegisterScreen
import dev.dereck.pc1moviles.ui.screens.SeguimientoScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val JUGADA = "jugada"
    const val SEGUIMIENTO = "seguimiento"
}

@Composable
fun AppNav(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()
    val start = if (user != null) Routes.JUGADA else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginOk = { navController.navigate(Routes.JUGADA) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onIrRegistro = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterOk = { navController.navigate(Routes.JUGADA) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onIrLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.JUGADA) {
            val jugadaVm: JugadaViewModel = viewModel()
            val current = user
            if (current == null) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginOk = { navController.navigate(Routes.JUGADA) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    onIrRegistro = { navController.navigate(Routes.REGISTER) }
                )
            } else {
                JugadaScreen(
                    email = current.email ?: "(sin correo)",
                    uid = current.uid,
                    viewModel = jugadaVm,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    },
                    onAvanzarSeguimiento = { navController.navigate(Routes.SEGUIMIENTO) }
                )
            }
        }
        composable(Routes.SEGUIMIENTO) {
            val current = user
            val segVm: dev.dereck.pc1moviles.seguimiento.SeguimientoViewModel = viewModel()
            if (current != null) SeguimientoScreen(current.email ?: "", current.uid, segVm, { navController.popBackStack() }, { authViewModel.logout(); navController.navigate(Routes.LOGIN){ popUpTo(0){inclusive=true} } })
        }
    }
}
