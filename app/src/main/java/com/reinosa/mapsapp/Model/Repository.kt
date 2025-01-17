package com.reinosa.mapsapp.Model

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Repository {
    private val database = FirebaseFirestore.getInstance()

    fun getMarkers():CollectionReference{
        return database.collection("markers")
    }

    fun getUserImageUri():CollectionReference{
        return database.collection("user")
    }

}