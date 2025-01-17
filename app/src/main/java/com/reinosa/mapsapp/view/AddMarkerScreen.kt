package com.reinosa.mapsapp.view


import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.reinosa.mapsapp.MapViewModel
import com.reinosa.mapsapp.Model.Categoria
import com.reinosa.mapsapp.Model.Marker
import com.reinosa.mapsapp.Routes

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddMarkerScreen(
    mapViewModel: MapViewModel,
    navController: NavController,
    onCloseBottomSheet: () -> Unit,
    estoyListScreen: Boolean
) {
    val categories: List<Categoria> by mapViewModel.categories.observeAsState(emptyList())
    val texto: String by mapViewModel.textoDropdownCategoria.observeAsState("Selecciona una categoría")

    val showGuapo: Boolean by mapViewModel.showGuapo.observeAsState(false)

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    Column(Modifier.fillMaxHeight(1f)) {

        if (!mapViewModel.userLogged()) {
            mapViewModel.signOut(context = LocalContext.current, navController)
        }

            // Mostrar la vista de captura de foto si no se ha tomado ninguna foto
            if (showGuapo) {
                if (estoyListScreen) {
                    navController.navigate(Routes.TakePhotoScreen.route)
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TakePhotoScreen(
                                mapViewModel = mapViewModel
                            ) { photo ->
                                // Aquí puedes manejar la foto capturada, por ejemplo, actualizar el estado o realizar otras acciones necesarias
                                mapViewModel.modifyPhotoBitmap(photo)
                                mapViewModel.modifyShowGuapo(false)
                            }
                        }
                    }
                }
            } else {
                // Mostrar la vista de agregar marcador después de tomar una foto
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = mapViewModel.getTitle(),
                        onValueChange = { mapViewModel.modifyTitle(it) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = mapViewModel.getSnippet(),
                        onValueChange = { mapViewModel.modifySnippet(it) },
                        label = { Text("Snippet") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { mapViewModel.modifyShowGuapo(true) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(text = "Hacer foto")
                    }

                    // Mostrar la imagen debajo del botón de hacer foto
                    val photoBitmap = mapViewModel.getPhotoBitmap()
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(), contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier
                                .clip(CircleShape)
                                .size(250.dp)
                                .background(Color.Blue)
                                .border(width = 1.dp, color = Color.White, shape = CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            //.background(Color(255f / 255, 0f / 255, 238f / 255, 0.1f))
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { /* No permitimos cambios directos aquí */ },
                            enabled = false,
                            readOnly = true,
                            modifier = Modifier
                                .clickable { mapViewModel.modifyExpanded(true) }
                                .fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = mapViewModel.pillarExpanded(),
                            onDismissRequest = { mapViewModel.modifyExpanded(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { categoria ->
                                DropdownMenuItem(text = { Text(text = categoria.name) }, onClick = {
                                    mapViewModel.modifySelectedCategory(categoria)
                                    mapViewModel.modifyExpanded(false)
                                    mapViewModel.modificarTextoDropdownCat(categoria.name)
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    var show by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            if (mapViewModel.getSelectedCategory() == null || !mapViewModel.getPhotoTaken() || mapViewModel.getTitle() == "") {
                                show = true
                            } else {
                                val categoryToAdd = mapViewModel.getSelectedCategory()!!
                                val latLng = if (mapViewModel.pillarEditingPosition() == null){ // Si la editing position es null es que no hemos dejado presionado
                                    mapViewModel.getPosition() // Como no lo hemos dejado presionado pillamos la position en la que estemos
                                } else mapViewModel.pillarEditingPosition() // Si no es nulo es que hemos presionado y nos quedamos con esa

                                val photo = mapViewModel.getPhotoBitmap()
                                val markerToAdd =
                                    photo?.let {
                                        Marker(
                                            mapViewModel.pillarLoggedUser(),
                                            null,
                                            latLng!!.latitude,
                                            latLng.longitude,
                                            mapViewModel.getTitle(),
                                            mapViewModel.getSnippet(),
                                            categoryToAdd,
                                            it,
                                            null
                                        )
                                    }
                                if (markerToAdd != null) {
                                    mapViewModel.addMarkerToDatabase(markerToAdd)
                                }
                                onCloseBottomSheet()
                                resetearParametros(mapViewModel)
                                mapViewModel.modificarTextoDropdownCat("Seleccionar Categoría")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Marker")
                    }
                    MyDialog(show) { show = false }
                }
            }
    }
}


@Composable
fun MyDialog(show: Boolean, onDismiss: () -> Unit) {
    if (show) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                Modifier
                    .background(Color.White)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Faltan valores!")
            }
        }
    }
}