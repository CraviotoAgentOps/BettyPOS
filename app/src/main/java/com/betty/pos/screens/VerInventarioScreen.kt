package com.betty.pos.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun VerInventarioScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var productos by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        try {
            val snapshot = db.collection("productos").get().await()
            productos = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
        } catch (e: Exception) {
            println("❌ Error cargando productos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Inventario", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Buscar producto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val filtrados = productos.filter {
                    val texto = searchText.trim().lowercase()
                    texto.isBlank() || (it["nombre"].toString().lowercase().contains(texto) ||
                            it["identificador"].toString().lowercase().contains(texto))
                }

                if (filtrados.isEmpty()) {
                    Text("No hay productos para mostrar.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtrados) { producto ->
                            ProductCard(producto) {
                                scope.launch {
                                    try {
                                        db.collection("productos").document(producto["id"].toString()).delete().await()
                                        productos = productos.filter { it["id"] != producto["id"] }
                                    } catch (e: Exception) {
                                        println("❌ Error al eliminar: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(producto: Map<String, Any>, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🛒 ${producto["nombre"] ?: "-"}", fontSize = 18.sp)
            Text("🔖 ID: ${producto["identificador"] ?: "-"}", fontSize = 14.sp)
            Text("💰 Costo: ${producto["costo"] ?: "-"}", fontSize = 14.sp)
            Text("💸 Precio 1: ${producto["precio1"] ?: "-"}", fontSize = 14.sp)
            Text("💸 Precio 2: ${producto["precio2"] ?: "-"}", fontSize = 14.sp)
            Text("📈 Margen 1: ${producto["margen1"] ?: "-"}%", fontSize = 12.sp)
            Text("📈 Margen 2: ${producto["margen2"] ?: "-"}%", fontSize = 12.sp)

            producto["imagenUrl"]?.let { url ->
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* TODO: navegación a pantalla de edición */ }) {
                    Text("Editar")
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            }
        }
    }
}