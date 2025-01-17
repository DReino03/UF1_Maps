package com.reinosa.mapsapp.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.ui.graphics.vector.ImageVector
import com.reinosa.mapsapp.Routes

sealed class Screens(val route: String, val title: String) {

    sealed class DrawerScreens(
        route: String,
        val icon: ImageVector,
        title: String
    ) : Screens(route, title) {
        //object Home : DrawerScreens(Routes.MenuScreen.route, Icons.Filled.Home, "Home")
        object Mapa : DrawerScreens(Routes.MapScreen.route, Icons.Filled.Home, "Mapa")
        object Listar :
            DrawerScreens(Routes.ListMarkersScreen.route, Icons.Filled.List, "Listar marcadores")

        object ProfileScreen : DrawerScreens(
            Routes.ProfileScreen.route,
            Icons.Filled.PersonPin,
            "Ver detalles usuario"
        )

        object CerrarSesion : DrawerScreens("cerrar_sesion", Icons.Filled.Close, "Cerrar Sesión")
    }
}

val screensFromDrawer = listOf(
    //Screens.DrawerScreens.Home,
    Screens.DrawerScreens.Mapa,
    Screens.DrawerScreens.Listar,
    Screens.DrawerScreens.ProfileScreen,
    Screens.DrawerScreens.CerrarSesion,
)