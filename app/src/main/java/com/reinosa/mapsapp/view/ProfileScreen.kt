package com.reinosa.mapsapp.view


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.reinosa.mapsapp.MapViewModel
import com.reinosa.mapsapp.MyDrawer
import com.reinosa.mapsapp.R


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileScreen(navController: NavController, mapViewModel: MapViewModel) {
    val imageUrl: String? by mapViewModel.imageUrlForUser.observeAsState(null)
    val loggedUser: String by mapViewModel.loggedUser.observeAsState("")
    val userName = loggedUser
    val nombre: String by mapViewModel.nombreUsuario.observeAsState(initial = "")
    mapViewModel.getProfileImageUrlForUser()

    if (!mapViewModel.userLogged()) {
        mapViewModel.signOut(context = LocalContext.current, navController)
    }

    val isLoading: Boolean by mapViewModel.isLoadingMarkers.observeAsState(initial = false)

    if (!isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    } else {
    MyDrawer(navController = navController, mapViewModel = mapViewModel) {
            // Mostrar la pantalla de captura de foto si showTakePhotoScreen es verdadero
            if (mapViewModel.showTakePhotoScreen) {
                Box(
                    modifier = Modifier.fillMaxSize() // Esto hará que la pantalla de captura de foto ocupe toda la pantalla
                ) {
                    TakePhotoScreen(
                        mapViewModel = mapViewModel
                    ) { photo ->
                        mapViewModel.modificarEditedProfilePhoto(photo)
                        mapViewModel.modificarShowTakePhotoScreen(false)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Gmail: $userName",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "User: $nombre",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(200.dp)
                            .clip(CircleShape) // Clip con CircleShape
                    ) {
                        if (mapViewModel.editedProfilePhoto != null) {
                            Image(
                                bitmap = mapViewModel.editedProfilePhoto!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imageUrl != null) {
                                println("YO NO ENTENDER" + mapViewModel.imageUrlForUser.value)
                                GlideImage(
                                    model = imageUrl,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_background),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                    }

                    // Botón para editar la foto del marcador
                    Button(
                        onClick = {
                            mapViewModel.modificarShowTakePhotoScreen(true)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 20.dp)
                    ) {
                        Text("Edit Photo")
                    }

                    Button(
                        onClick = {
                            mapViewModel.updateUser()
                            mapViewModel.modificarEditedProfilePhoto(null)
                            //mapViewModel.getProfileImageUrlForUser()
                            //navController.navigate(Routes.ProfileScreen.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 5.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
