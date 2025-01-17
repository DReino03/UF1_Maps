package com.reinosa.mapsapp.Model

import android.graphics.Bitmap

data class Marker(
    var owner:String?,
    var markerId:String?,
    var latitude:Double,
    var longitude:Double,
    var title:String,
    var snippet:String,
    var category: Categoria,
    var photo: Bitmap?,
    var photoReference:String?
){
    constructor():this(null,null,0.0,0.0,"","",Categoria(""),null,null)

    // Método para modificar el título
    fun modificarTitle(newTitle: String) {
        title = newTitle
    }

    // Método para modificar el fragmento
    fun modificarSnippet(newSnippet: String) {
        snippet = newSnippet
    }

    // Método para modificar la foto
    fun modificarPhoto(newPhoto: Bitmap) {
        photo = newPhoto
    }

    fun modificarPhotoReference(newReference: String) {
        photoReference = newReference
    }

    fun modificarCategoria(newReference: String) {
        category.name = newReference
    }
}