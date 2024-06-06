package com.reinosa.mapsapp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.reinosa.mapsapp.Model.Categoria
import com.reinosa.mapsapp.Model.Marker
import com.reinosa.mapsapp.Model.Repository
import com.reinosa.mapsapp.Model.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.maps.model.LatLng


class MapViewModel : ViewModel() {

    var editedTitle by mutableStateOf("")
        private set

    var editedSnippet by mutableStateOf("")
        private set

    var editedCategoryName by mutableStateOf("")
        private set

    var editedPhoto by mutableStateOf<Bitmap?>(null)
        private set

    var editedProfilePhoto by mutableStateOf<Bitmap?>(null)
        private set

    var showTakePhotoScreen by mutableStateOf(false)
        private set

    var showDialog by mutableStateOf(false)
        private set

    fun modificarEditedTitle(title: String) {
        editedTitle = title
    }

    fun modificarEditedSnippet(snippet: String) {
        editedSnippet = snippet
    }

    fun modificarEditedProfilePhoto(photo: Bitmap?) {
        editedProfilePhoto = photo
    }
    fun modificarEditedPhoto(photo: Bitmap?) {
        editedPhoto = photo
    }

    fun modificarCategoryName(categoryName: String) {
        editedCategoryName = categoryName
    }

    fun modificarShowTakePhotoScreen(value: Boolean) {
        showTakePhotoScreen = value
    }

    fun modificarShowDialog(value: Boolean) {
        showDialog = value
    }

    private val title = mutableStateOf("")
    private val snippet = mutableStateOf("")
    private val selectedCategoria = mutableStateOf<Categoria?>(null)
    private val photoBitmap = mutableStateOf<Bitmap?>(null)
    private val _uriFoto = mutableStateOf<Uri?>(null)
    private var uriFoto = _uriFoto

    private val photoTaken = mutableStateOf(false)
    private val _showGuapo = MutableLiveData<Boolean>(false)
    val showGuapo = _showGuapo

    private val _cameraPermissionGranted = MutableLiveData(false)
    val cameraPositionGranted = _cameraPermissionGranted

    private val _shouldShowPermissionRationale = MutableLiveData(false)
    val shouldShowPermissionRationale = _shouldShowPermissionRationale

    private val _showPermissionDenied = MutableLiveData(false)
    val showPermissionDenied = _showPermissionDenied

    private val database = FirebaseFirestore.getInstance()

    // LiveData para la lista de marcadores
    private val _markers = MutableLiveData<MutableList<Marker>>()
    val markers: LiveData<MutableList<Marker>> = _markers

    private var repository: Repository = Repository()

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun setShouldShowPermissionRationale(should: Boolean) {
        _shouldShowPermissionRationale.value = should
    }

    fun setShowPermissionDenied() {
        _showPermissionDenied
    }

    fun modifyTitle(newValue: String) {
        title.value = newValue
    }

    fun getTitle(): String {
        return title.value
    }

    fun modifySnippet(newValue: String) {
        snippet.value = newValue
    }

    fun getSnippet(): String {
        return snippet.value
    }

    fun modifySelectedCategory(newValue: Categoria?) {
        selectedCategoria.value = newValue
    }

    fun getSelectedCategory(): Categoria? {
        return selectedCategoria.value
    }

    fun modifyUriPhoto(newValue: Uri?) {
        _uriFoto.value = newValue
    }

    fun modifyPhotoBitmap(newValue: Bitmap?) {
        photoBitmap.value = newValue
    }

    fun getPhotoBitmap(): Bitmap? {
        return photoBitmap.value
    }

    fun modifyPhotoTaken(newValue: Boolean) {
        photoTaken.value = newValue
    }

    fun getPhotoTaken(): Boolean {
        return photoTaken.value
    }

    fun modifyShowGuapo(newValue: Boolean) {
        showGuapo.value = newValue
    }

    fun getShowGuapo(): Boolean? {
        return showGuapo.value
    }

    private var expanded by mutableStateOf(false)

    fun modifyExpanded(valorNuevo: Boolean) {
        expanded = valorNuevo
    }

    fun pillarExpanded(): Boolean {
        return expanded
    }

    private var expandedMapa by mutableStateOf(false)

    fun modifyExpandedMapa(valorNuevo: Boolean) {
        expandedMapa = valorNuevo
    }

    fun pillarExpandedMapa(): Boolean {
        return expandedMapa
    }

    private var position = LatLng(41.4534265, 2.1837151)
    fun changePosition(positionNueva: LatLng) {
        position = positionNueva
    }

    fun getPosition(): LatLng {
        return position
    }

    private var _editingPosition = MutableLiveData<LatLng?>()
    val editingPosition = _editingPosition

    fun modificarEditingPosition(newValue: LatLng?){
        _editingPosition.value = newValue
    }

    fun pillarEditingPosition(): LatLng? {
        return _editingPosition.value
    }

    // LiveData para la lista de marcadores
    private val _editingMarkers = MutableLiveData<Marker>()
    var editingMarkers: LiveData<Marker> = _editingMarkers

    fun setEditingMarkers(marker: Marker) {
        _editingMarkers.value = marker
    }

    private val _categories = MutableLiveData<MutableList<Categoria>>()
    val categories: LiveData<MutableList<Categoria>> = _categories

    private val _textoDropDown = MutableLiveData<String>()
    val textoDropdown: LiveData<String> = _textoDropDown

    private val _textoDropDownCategorias = MutableLiveData<String>()
    val textoDropdownCategoria: LiveData<String> = _textoDropDownCategorias


    private val _showBottomSheet = MutableLiveData<Boolean>()
    val showBottomSheet = _showBottomSheet

    fun modificarShowBottomSheet(nuevoBoolean: Boolean) {
        _showBottomSheet.value = nuevoBoolean
    }

    fun modificarTextoDropdown(nuevoTexto: String) {
        _textoDropDown.value = nuevoTexto
    }

    fun modificarTextoDropdownCat(nuevoTexto: String) {
        _textoDropDownCategorias.value = nuevoTexto
    }

    init {
        // Inicializar la lista de categorías si es necesario
        if (_categories.value == null) {
            _categories.value = mutableListOf(
                Categoria("Info"),
                Categoria("Likes"),
                Categoria("Favoritos")
            )
        }
    }

    // Método para obtener la lista de categorías
    fun getCategories(): List<Categoria> {
        return _categories.value.orEmpty()
    }

    fun deleteMarker(markerId: String) {
        database.collection("markers").document(markerId).delete()
    }

    fun updateMarker(editedMarker: Marker) {
        if (uriFoto.value != null) {
            modifyLoadingMarkers(false)

            // Obtener la URL de la foto actual del marcador
            val oldImageUrl = editedMarker.photoReference
            // Subir la nueva imagen y actualizar el marcador
            uploadImage(uriFoto.value!!) { downloadUrl ->
                // Actualizar la referencia de la foto en el marcador con la URL de descarga
                editedMarker.modificarPhotoReference(downloadUrl)

                // Agregar el marcador a la base de datos con la referencia de la foto actualizada
                database.collection("markers").document(editedMarker.markerId!!)
                    .set(
                        hashMapOf(
                            "owner" to _loggedUser.value,
                            "positionLatitude" to editedMarker.latitude,
                            "positionLongitude" to editedMarker.longitude,
                            "title" to editedMarker.title,
                            "snippet" to editedMarker.snippet,
                            "categoryName" to editedMarker.category.name,
                            "linkImage" to editedMarker.photoReference
                        )
                    )
                    .addOnSuccessListener {
                        Log.d("Success",("Marker añadido correctamente a la base de datos"))
                        // Eliminar la foto anterior del almacenamiento si existe
                        if (oldImageUrl != null) {
                            deleteProfileImage(oldImageUrl)
                        }
                        // Solicitar la lista completa de marcadores después de añadir uno nuevo
                        pillarTodosMarkers()
                        modifyLoadingMarkers(true)

                    }
                    .addOnFailureListener { e ->
                        Log.d("Error",("Error al añadir el marker a la base de datos: ${e.message}"))
                        modifyLoadingMarkers(true)
                    }

            }
        } else {
            // El caso cuando no hay una nueva imagen
            // Agregar el marcador a la base de datos sin modificar la imagen
            database.collection("markers").document(editedMarker.markerId!!)
                .set(
                    hashMapOf(
                        "owner" to _loggedUser.value,
                        "positionLatitude" to editedMarker.latitude,
                        "positionLongitude" to editedMarker.longitude,
                        "title" to editedMarker.title,
                        "snippet" to editedMarker.snippet,
                        "categoryName" to editedMarker.category.name,
                        "linkImage" to editedMarker.photoReference
                    )
                )
                .addOnSuccessListener {
                    Log.d("Success",("Marker añadido correctamente a la base de datos"))
                    // Solicitar la lista completa de marcadores después de añadir uno nuevo
                    pillarTodosMarkers()
                    modifyLoadingMarkers(true)
                }
                .addOnFailureListener { e ->
                    Log.d("Error",("Error al añadir el marker a la base de datos: ${e.message}"))
                    modifyLoadingMarkers(true)
                }
        }
    }

    fun addMarkerToDatabase(marker: Marker) {
        uploadImage(uriFoto.value!!) { downloadUrl ->
            // Actualizar la referencia de la foto en el marcador con la URL de descarga
            marker.modificarPhotoReference(downloadUrl)

            // Agregar el marcador a la base de datos con la referencia de la foto actualizada
            database.collection("markers")
                .add(
                    hashMapOf(
                        "owner" to _loggedUser.value,
                        "positionLatitude" to marker.latitude,
                        "positionLongitude" to marker.longitude,
                        "title" to marker.title,
                        "snippet" to marker.snippet,
                        "categoryName" to marker.category.name,
                        "linkImage" to marker.photoReference
                    )
                )
                .addOnSuccessListener {
                    Log.d("Success",("Marker añadido correctamente a la base de datos"))
                    // Solicitar la lista completa de marcadores después de añadir uno nuevo
                    pillarTodosMarkers()
                    modifyLoadingMarkers(true)
                }
                .addOnFailureListener { e ->
                    Log.d("Error",("Error al añadir el marker a la base de datos: ${e.message}"))
                }
        }
    }

    private fun uploadImage(imageUri: Uri, onComplete: (String) -> Unit) {
        Log.d("Inicio",("XAVI UWU"))
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = java.util.Date()
        val fileName = formatter.format(now)
        val storage = FirebaseStorage.getInstance().getReference("images/$fileName")
        modifyLoadingMarkers(false)

        storage.putFile(imageUri)
            .addOnSuccessListener { uploadTask ->
                Log.i("IMAGE UPLOAD", "Image uploaded successfully")
                Log.d("Success",("Xavi peruano fino"))
                uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Log.d("Success",("URL de descarga de la imagen: $downloadUrl"))
                    onComplete(downloadUrl) // Llamar a la funcion que le pasamos, en este caso le pasamos la de añadir marcador a la bbdd
                }
            }
            .addOnFailureListener {
                Log.i("IMAGE UPLOAD", "Image upload failed")
                Log.d("Error",("Xavi peruano malo"))
                modifyLoadingMarkers(true)
            }
    }

    fun pillarTodosMarkers() {
        repository.getMarkers()
            .whereEqualTo("owner", _loggedUser.value)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return@addSnapshotListener
                }
                val tempList = mutableListOf<Marker>()
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val newMarker = dc.document.toObject(Marker::class.java)
                        newMarker.markerId = dc.document.id
                        newMarker.latitude =
                            dc.document.get("positionLatitude").toString().toDouble()
                        newMarker.longitude =
                            dc.document.get("positionLongitude").toString().toDouble()
                        newMarker.category.name = dc.document.get("categoryName").toString()
                        newMarker.photoReference = dc.document.get("linkImage").toString()
                        tempList.add(newMarker)
                        Log.d("Success",("Adios :( $newMarker"))
                    }

                }
                _markers.value = tempList
            }
    }

    fun pillarTodosMarkersCategoria(categoria: String) {
        repository.getMarkers()
            .whereEqualTo("owner", _loggedUser.value)
            .whereEqualTo("categoryName", categoria)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return@addSnapshotListener
                }
                val tempList = mutableListOf<Marker>()
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val newMarker = dc.document.toObject(Marker::class.java)
                        newMarker.markerId = dc.document.id
                        newMarker.latitude =
                            dc.document.get("positionLatitude").toString().toDouble()
                        newMarker.longitude =
                            dc.document.get("positionLongitude").toString().toDouble()
                        newMarker.category.name = dc.document.get("categoryName").toString()
                        newMarker.photoReference = dc.document.get("linkImage").toString()
                        tempList.add(newMarker)
                        Log.d("Success",("Adios :( " + newMarker.category.name))
                    }

                }
                _markers.value = tempList
            }
    }

    private val auth = FirebaseAuth.getInstance()

    fun userLogged(): Boolean {
        return auth.currentUser != null
    }

    // LiveData para isLoading
    private val _goToNext = MutableLiveData<Boolean>()
    val goToNext = _goToNext

    private val _userId = MutableLiveData<String>()
    private val _loggedUser = MutableLiveData<String>()
    val loggedUser = _loggedUser
    fun pillarLoggedUser(): String {
        return _loggedUser.value.toString()
    }

    private val _isLoading = MutableLiveData(true)
    val isLoading = _isLoading

    private val _isLoadingMarkers = MutableLiveData(true)
    val isLoadingMarkers = _isLoadingMarkers

    fun modifyLoadingMarkers(newValue: Boolean){
        _isLoadingMarkers.value = newValue
    }

    fun modifyProcessing(newValue: Boolean) {
        _isLoading.value = newValue
    }



    private val _imageUrlForUser = MutableLiveData<String?>()
    val imageUrlForUser = _imageUrlForUser

    private val _nombreUsuario = MutableLiveData<String>()
    val nombreUsuario = _nombreUsuario

    fun getProfileImageUrlForUser() {
        repository.getUserImageUri()
            .whereEqualTo("owner", _loggedUser.value)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return@addSnapshotListener
                }

                var tempString : String? = null

                var tempStringNombre = "¿?"
                if (value != null) {
                    for (dc: DocumentChange in value.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            tempString = dc.document.getString("image") ?: tempString
                            tempStringNombre = (dc.document.getString("name") ?: tempString).toString()
                            Log.d("Success",("Un peruano encontrado"))
                            Log.d("Success",(tempString.toString()))
                        }
                    }
                }
                _nombreUsuario.value = tempStringNombre
                _imageUrlForUser.value = tempString
            }
    }

    fun updateUser() {
        getProfileImageUrlForUser()
        var oldImageUrl:String? = null
        if (_imageUrlForUser.value != null){
            oldImageUrl = _imageUrlForUser.value // Guarda la URL de la imagen actual
        }

        uploadImage(uriFoto.value!!) { downloadUrl ->
            // Realizar una consulta para encontrar el documento del usuario
            database.collection("user")
                .whereEqualTo("owner", _loggedUser.value)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Actualizar el documento con la nueva información
                        val data = mutableMapOf<String, Any>("image" to downloadUrl)
                        document.reference.update(data)
                            .addOnSuccessListener {
                                Log.d("Success",("Usuario actualizado correctamente en la base de datos"))

                                // Eliminar la imagen anterior del almacenamiento
                                if (oldImageUrl != null) {
                                    deleteProfileImage(oldImageUrl)
                                }
                                modifyLoadingMarkers(true)
                            }
                            .addOnFailureListener { e ->
                                Log.d("Error",("Error al actualizar el usuario en la base de datos: ${e.message}"))
                            }
                        modifyLoadingMarkers(true)
                    }
                    getProfileImageUrlForUser()
                }
                .addOnFailureListener { exception ->
                    Log.d("Error",("Error al consultar la base de datos: ${exception.message}"))
                    modifyLoadingMarkers(true)
                }
        }
    }

    private fun deleteProfileImage(imageUrl: String) {
        // Obtener la referencia de Firebase Storage
        val storageRef = Firebase.storage.reference

        // Obtener la referencia de la imagen a eliminar usando la URL
        val imageRef = storageRef.storage.getReferenceFromUrl(imageUrl)

        // Eliminar la imagen
        imageRef.delete()
            .addOnSuccessListener {
                Log.d("Success",("Imagen anterior del perfil eliminada correctamente del almacenamiento"))
            }
            .addOnFailureListener { e ->
                Log.d("Error",("Error al eliminar la imagen anterior del perfil del almacenamiento: ${e.message}"))
            }
    }

    fun signInWithGoogleCredential(credential: AuthCredential, home: () -> Unit) =
        viewModelScope.launch {
            modifyProcessing(false)
            try {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Success", "Log con exito")
                            val userRef =
                                database.collection("user").whereEqualTo("owner", _loggedUser.value)
                            userRef.get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        // Si no hay documentos para este usuario, agregar uno nuevo
                                        database.collection("user")
                                            .add(
                                                hashMapOf(
                                                    "owner" to _loggedUser.value,
                                                    "name" to (_loggedUser.value?.split("@")?.get(0) ?: ""),
                                                    // "apellido" to _apellidoState.value,
                                                    // "ciudad" to _ciudadState.value,
                                                    // "password" to usuari.password (es logico guardar la contraseña rarete, no?)
                                                )
                                            )
                                    }
                                }
                            home()
                        }
                    }
                    .addOnFailureListener {
                        Log.d("Error", "Fallo al loguear")
                    }
            } catch (ex: Exception) {
                Log.d("ExcepcionGoogle", "Excepción al hacer log" + ex.localizedMessage)
            }
        }
}