package com.reinosa.mapsapp.view

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.reinosa.mapsapp.MapViewModel
import com.reinosa.mapsapp.R
import com.reinosa.mapsapp.Routes
import java.io.OutputStream

@Composable
fun Camara(navigationController: NavController, mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val isCameraPermissionGranted by mapViewModel.cameraPositionGranted.observeAsState(false)
    val shouldShowPermissionRationale by mapViewModel.shouldShowPermissionRationale.observeAsState(
        false
    )
    val showPermissionDenied by mapViewModel.showPermissionDenied.observeAsState(false)

    if (!mapViewModel.userLogged()) {
        mapViewModel.signOut(context = LocalContext.current, navigationController)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                mapViewModel.setCameraPermissionGranted(true)
            } else {
                mapViewModel.setShouldShowPermissionRationale(
                    shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.CAMERA
                    )
                )
                if (!shouldShowPermissionRationale) {
                    Log.i("CameraScreen", "No podemos volver a pedir permisos")
                    mapViewModel.setShowPermissionDenied()
                }
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            if (!isCameraPermissionGranted) {
                launcher.launch(Manifest.permission.CAMERA)
            } else {
                navigationController.navigate(Routes.TakePhotoScreen.route)
            }
        }) {
            Text(text = "Take photo")
        }
    }
    if (showPermissionDenied) {
        PermissionDeclinedScreen()
    }

}

@Composable
fun PermissionDeclinedScreen() {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Permission required", fontWeight = FontWeight.Bold)
        Text(text = "This app needs access to the camera to take photos")
        Button(onClick = { openAppSettings(context as Activity) }) {
            Text(text = "Accept")
        }
    }
}

fun openAppSettings(activity: Activity) {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", activity.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    activity.startActivity(intent)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TakePhotoScreen(
    mapViewModel: MapViewModel,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }
    if (permissionState.status.isGranted) {
        val context = LocalContext.current

        val controller = remember {
            LifecycleCameraController(context).apply {
                CameraController.IMAGE_CAPTURE
            }
        }
        val img: Bitmap? = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)?.toBitmap()
        var bitmap by remember { mutableStateOf(img) }

        val launchImage = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = {
                if (it != null) {
                    val uri = it

                    bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    } else {
                        val source = it.let { it1 ->
                            ImageDecoder.createSource(context.contentResolver, it1)
                        }
                        source.let { it1 ->
                            ImageDecoder.decodeBitmap(it1)
                        }
                    }

                    mapViewModel.modifyUriPhoto(uri)
                    // Guardar la imagen en el ViewModel
                    bitmap?.let {
                        mapViewModel.modifyPhotoBitmap(it)
                        onPhotoCaptured(it) // Llamar a la función onPhotoCaptured
                    }
                    // mapViewModel.modifyShowGuapo(false)
                    mapViewModel.modifyPhotoTaken(true) // Actualizar el estado cuando se toma la foto
                } else {
                    // Manejar el caso donde no se selecciona una imagen
                    Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT)
                        .show()
                    mapViewModel.modifyShowGuapo(false)
                }
            })


        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                },
                modifier = Modifier.offset(
                    16.dp,
                    16.dp
                )
            ) {
                Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = {
                            launchImage.launch("image/*")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Photo, contentDescription = "Open gallery")
                    }
                    IconButton(onClick = {
                        takePhoto(context, mapViewModel, controller) { photo ->
                            mapViewModel.modifyPhotoBitmap(photo)
                            mapViewModel.modifyShowGuapo(false)
                            mapViewModel.modifyPhotoTaken(true) // Actualizar el estado cuando se toma la foto
                            onPhotoCaptured(photo) // Llamar a la función onPhotoCaptured
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Camera, contentDescription = "Take photo")
                    }
                }
            }
        }
    } else {
        PermissionDeclinedScreen()
    }

}

fun takePhoto(
    context: Context,
    mapViewModel: MapViewModel,
    controller: LifecycleCameraController, onPhotoTaken: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                onPhotoTaken(image.toBitmap())
                mapViewModel.modifyUriPhoto(bitmapToUri(context, image.toBitmap()))
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Error taken photo", exception)
            }
        }
    )
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val filename = "${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, filename)
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    }

    val uri: Uri? =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        val outstream: OutputStream? = context.contentResolver.openOutputStream(it)
        outstream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        outstream?.close()
    }

    return uri
}

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = {
        PreviewView(it).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
        }
    }, modifier = modifier)
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TakePhotoScreen(
    navigationController: NavController,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }
    if (permissionState.status.isGranted) {
        val controller = remember {
            LifecycleCameraController(context).apply {
                CameraController.IMAGE_CAPTURE
            }
        }
        val img: Bitmap? = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)?.toBitmap()
        var bitmap by remember { mutableStateOf(img) }

        val launchImage = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = {
                if (it != null) {
                    val uri = it

                    bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    } else {
                        val source = it.let { it1 ->
                            ImageDecoder.createSource(context.contentResolver, it1)
                        }
                        source.let { it1 ->
                            ImageDecoder.decodeBitmap(it1)
                        }
                    }

                    mapViewModel.modifyUriPhoto(uri)
                    // Guardar la imagen en el ViewModel
                    bitmap?.let {
                        mapViewModel.modifyPhotoBitmap(it)
                        mapViewModel.modifyShowGuapo(false)
                    }
                    // mapViewModel.modifyShowGuapo(false)
                    mapViewModel.modifyPhotoTaken(true) // Actualizar el estado cuando se toma la foto
                    navigationController.navigate(Routes.MapScreen.route) // Esta línea navega a la pantalla anterior
                } else {
                    // Manejar el caso donde no se selecciona una imagen
                    Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT)
                        .show()
                    mapViewModel.modifyShowGuapo(false)
                }
            })


        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                },
                modifier = Modifier.offset(
                    16.dp,
                    16.dp
                )
            ) {
                Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = {
                            launchImage.launch("image/*")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Photo, contentDescription = "Open gallery")
                    }
                    IconButton(onClick = {
                        takePhoto(context, mapViewModel, controller) { photo ->
                            mapViewModel.modifyPhotoBitmap(photo)
                            mapViewModel.modifyShowGuapo(false)
                            mapViewModel.modifyPhotoTaken(true) // Actualizar el estado cuando se toma la foto
                            navigationController.navigate(Routes.MapScreen.route) // Esta línea navega a la pantalla anterior
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Camera, contentDescription = "Take photo")
                    }
                }
            }
        }
    } else {
        PermissionDeclinedScreen()
    }

}
