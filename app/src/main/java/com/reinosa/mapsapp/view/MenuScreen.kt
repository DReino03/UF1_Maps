package com.reinosa.mapsapp.view


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.reinosa.mapsapp.MapViewModel
import com.reinosa.mapsapp.MyDrawer
import com.reinosa.mapsapp.Routes

// No tiene sentido usarlo ya que tenemos el drawer
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MenuScreen(mapViewModel: MapViewModel, navController: NavController) {

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        MyDrawer(
            navController,
            mapViewModel = mapViewModel,
            content = {
                if (!mapViewModel.userLogged()){
                    mapViewModel.signOut(context = LocalContext.current, navController)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Bienvenido al Menú",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Usuario: ${mapViewModel.pillarLoggedUser().split("@")[0]}",
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 16.dp))
                    Column(
                        Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                navController.navigate(Routes.MapScreen.route)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(text = "Mapa")
                        }
                        Button(
                            onClick = {
                                navController.navigate(Routes.ListMarkersScreen.route)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(text = "Listar marcadores")
                        }
                        val context = LocalContext.current

                        Button(
                            onClick = {
                                mapViewModel.signOut(context,navController)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(text = "Cerrar Sesión")
                        }
                    }
                }

            }
        )
    }
}