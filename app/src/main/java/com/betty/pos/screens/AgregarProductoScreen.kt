package com.betty.pos.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

@Composable
fun AgregarProductoScreen(navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    var nombre by remember { mutableStateOf("") }
    var identificador by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var precio1 by remember { mutableStateOf("") }
    var precio2 by remember { mutableStateOf("") }
    var margen1 by remember { mutableStateOf(0.0) }
    var margen2 by remember { mutableStateOf(0.0) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    fun createImageFile(context: Context): File? {
        return try {
            File.createTempFile("producto_${System.currentTimeMillis()}", ".jpg", context.externalCacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun calcularMargen(costo: String, precio: String): Double {
        val c = costo.toDoubleOrNull() ?: return 0.0
        val p = precio.toDoubleOrNull() ?: return 0.0
        return if (c > 0) ((p - c) / c) * 100 else 0.0
    }

    fun limpiarCampos() {
        nombre = ""
        identificador = ""
        costo = ""
        precio1 = ""
        precio2 = ""
        margen1 = 0.0
        margen2 = 0.0
        imageBitmap = null
        imageUri = null
    }

    fun guardarProducto() {
        if (nombre.isBlank() || identificador.isBlank() || costo.isBlank() || precio1.isBlank()) {
            Toast.makeText(context, "Por favor completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true

        val producto = hashMapOf(
            "nombre" to nombre,
            "identificador" to identificador,
            "costo" to costo.toDoubleOrNull(),
            "precio1" to precio1.toDoubleOrNull(),
            "precio2" to precio2.toDoubleOrNull(),
            "margen1" to margen1,
            "margen2" to margen2,
            "timestamp" to Date()
        )

        fun subirDatos(imagenUrl: String?) {
            if (!imagenUrl.isNullOrBlank()) {
                producto["imagenUrl"] = imagenUrl
            }

            db.collection("productos")
                .add(producto)
                .addOnSuccessListener {
                    Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener {
                    isSaving = false
                }
        }

        imageBitmap?.let { bitmap ->
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()
            val safeId = UUID.randomUUID().toString()
            val imageRef = storageRef.child("productos/$safeId.jpg")

            imageRef.putBytes(data)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Fallo al subir imagen")
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    println("✅ Imagen subida: $uri")
                    Toast.makeText(context, "Imagen subida con éxito", Toast.LENGTH_SHORT).show()
                    subirDatos(uri.toString())
                }
                .addOnFailureListener { e ->
                    println("❌ Error al subir imagen:")
                    e.printStackTrace()
                    Toast.makeText(context, "Error subiendo imagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    subirDatos(null)
                }
        } ?: subirDatos(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoFile != null) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile!!)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                if (bitmap != null) {
                    imageBitmap = bitmap
                    imageUri = uri
                    Toast.makeText(context, "Foto cargada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se pudo obtener la imagen", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error procesando imagen de cámara", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = createImageFile(context)
            if (file != null) {
                photoFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "No se pudo crear archivo para la foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                imageBitmap = bitmap
                imageUri = it
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Agregar Producto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = identificador,
            onValueChange = { identificador = it },
            label = { Text("Identificador rápido (ej. gomi)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = costo,
            onValueChange = {
                costo = it
                margen1 = calcularMargen(costo, precio1)
                margen2 = calcularMargen(costo, precio2)
            },
            label = { Text("Costo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio1,
            onValueChange = {
                precio1 = it
                margen1 = calcularMargen(costo, precio1)
            },
            label = { Text("Precio 1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Margen 1: ${"%.2f".format(margen1)}%")

        OutlinedTextField(
            value = precio2,
            onValueChange = {
                precio2 = it
                margen2 = calcularMargen(costo, precio2)
            },
            label = { Text("Precio 2") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Margen 2: ${"%.2f".format(margen2)}%")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }) {
                Text("Abrir Cámara")
            }

            Button(onClick = {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Galería")
            }
        }

        imageBitmap?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Imagen del producto",
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (!isSaving) guardarProducto() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Guardando..." else "Guardar Producto")
        }
    }
}
