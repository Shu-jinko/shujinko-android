package com.shujinko.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth")

object TokenStore {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveAccessToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[ACCESS_TOKEN] = token }
    }

    suspend fun saveRefreshToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[REFRESH_TOKEN] = token }
    }

    fun getAccessToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { it[ACCESS_TOKEN] }
    }

    fun getRefreshToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { it[REFRESH_TOKEN] }
    }

    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}