package com.betty.pos.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.betty.pos.ui.components.MenuTopBar

@Composable
fun MenuVentasScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        MenuTopBar(title = "Ventas", navController = navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("punto_venta") }, modifier = Modifier.fillMaxWidth()) {
                Text("Punto de Venta")
            }
            Button(onClick = { navController.navigate("control_caja") }, modifier = Modifier.fillMaxWidth()) {
                Text("Control de Caja")
            }
            Button(onClick = { navController.navigate("corte_caja") }, modifier = Modifier.fillMaxWidth()) {
                Text("Corte de Caja")
            }
            Button(onClick = { navController.navigate("ver_tickets") }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Tickets")
            }
        }
    }
}