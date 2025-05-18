package com.shujinko.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth")

object TokenStore {
    private val JWT_TOKEN = stringPreferencesKey("jwt_token")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[JWT_TOKEN] = token }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { it[JWT_TOKEN] }
    }
}
