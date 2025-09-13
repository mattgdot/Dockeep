package com.app.dockeep.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_pref")

    override suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { pref ->
            pref[prefKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        context.dataStore.edit { pref ->
            pref[prefKey] = value
        }
    }

    override suspend fun putBool(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        context.dataStore.edit { pref ->
            pref[prefKey] = value
        }
    }

    override suspend fun getString(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        val pref = context.dataStore.data.first()
        return pref[prefKey]
    }

    override suspend fun getInt(key: String): Int? {
        val prefKey = intPreferencesKey(key)
        val pref = context.dataStore.data.first()
        return pref[prefKey]
    }

    override suspend fun getBool(key: String): Boolean? {
        val prefKey = booleanPreferencesKey(key)
        val pref = context.dataStore.data.first()
        return pref[prefKey]
    }
}