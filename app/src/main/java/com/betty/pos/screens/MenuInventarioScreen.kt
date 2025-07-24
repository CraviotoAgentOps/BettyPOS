package com.betty.pos.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.betty.pos.ui.components.MenuTopBar

@Composable
fun MenuInventarioScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MenuTopBar(title = "Inventario", navController = navController)

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("agregar_producto") }) {
                Text("Agregar Producto")
            }

            Button(onClick = { navController.navigate("modificar_existencias") }) {
                Text("Modificar Existencias")
            }

            Button(onClick = { navController.navigate("ver_inventario") }) {
                Text("Ver Inventario")
            }

            Button(onClick = { navController.navigate("ordenes_compra") }) {
                Text("Órdenes de Compra")
            }

            Button(onClick = { navController.navigate("reporte_ventas") }) {
                Text("Reporte de Ventas")
            }
        }
    }
}