package com.betty.pos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.betty.pos.screens.*
import com.betty.pos.ui.HomeScreen
import com.betty.pos.ui.SplashScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "splash") {

        // Pantallas base
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }

        // Menús principales
        composable("ventas_menu") { MenuVentasScreen(navController) }
        composable("inventario_menu") { MenuInventarioScreen(navController) }

        // Pantallas de Ventas
        composable("punto_venta") { PuntoVentaScreen(navController) }
        composable("corte_caja") { CorteCajaScreen(navController) }
        composable("control_caja") { ControlCajaScreen(navController) }
        composable("ver_tickets") { VerTicketsScreen(navController) }

        // Pantallas de Inventario
        composable("agregar_producto") { AgregarProductoScreen(navController) }
        composable("modificar_existencias") { ModificarExistenciasScreen(navController) }
        composable("ver_inventario") { VerInventarioScreen(navController) }
        composable("ordenes_compra") { OrdenesCompraScreen(navController) }
        composable("reporte_ventas") { ReporteVentasScreen(navController) }
    }
}