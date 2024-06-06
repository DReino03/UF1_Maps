package com.reinosa.mapsapp.Model
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPrefs(private val context: Context) {

    //CREACIO DE LA BASE DE DADES
    companion object{
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_perfs")
        val STORE_USERNAME = stringPreferencesKey("store_username")
        val STORE_USERPASS = stringPreferencesKey("store_userpass")
    }

    //GET DADES USUARI
    val getUserData: Flow<List<String>> = context.dataStore.data.map { prefs ->
        listOf(
            prefs[STORE_USERNAME] ?: "",
            prefs[STORE_USERPASS] ?: ""
        )
    }
    suspend fun saveUserData(username: String, userpass: String) {
        context.dataStore.edit { prefs ->
            prefs[STORE_USERNAME] = username
            prefs[STORE_USERPASS] = userpass
        }
    }

    suspend fun deleteUserData(){
        context.dataStore.edit { prefs ->
            prefs[STORE_USERNAME] = ""
            prefs[STORE_USERPASS] = ""
        }
    }

    suspend fun deleteUserPass(){
        context.dataStore.edit { prefs ->
            prefs[STORE_USERPASS] = ""
        }
    }

}