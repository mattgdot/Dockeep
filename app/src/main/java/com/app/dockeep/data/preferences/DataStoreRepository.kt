package com.app.dockeep.data.preferences

interface DataStoreRepository {
    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putBool(key: String, value: Boolean)
    suspend fun getString(key: String): String?
    suspend fun getInt(key: String): Int?
    suspend fun getBool(key: String): Boolean?
}