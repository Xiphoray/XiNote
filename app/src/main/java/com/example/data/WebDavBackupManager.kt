package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebDavBackupManager(private val context: Context, private val noteDao: NoteDao) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val noteListType = Types.newParameterizedType(List::class.java, Note::class.java)
    private val adapter = moshi.adapter<List<Note>>(noteListType)

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getWebDavConfig(): WebDavConfig {
        val sp = context.getSharedPreferences("webdav_settings", Context.MODE_PRIVATE)
        return WebDavConfig(
            url = sp.getString("url", "") ?: "",
            port = sp.getString("port", "") ?: "",
            username = sp.getString("username", "") ?: "",
            password = sp.getString("password", "") ?: "",
            path = sp.getString("path", "") ?: ""
        )
    }

    fun saveWebDavConfig(config: WebDavConfig) {
        val sp = context.getSharedPreferences("webdav_settings", Context.MODE_PRIVATE)
        sp.edit().apply {
            putString("url", config.url)
            putString("port", config.port)
            putString("username", config.username)
            putString("password", config.password)
            putString("path", config.path)
            apply()
        }
    }

    suspend fun backupToCloud(): Boolean = withContext(Dispatchers.IO) {
        val config = getWebDavConfig()
        if (config.url.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            return@withContext false
        }

        try {
            val notes = noteDao.getLatestNotes(10000)
            val jsonString = adapter.toJson(notes) ?: return@withContext false

            val credential = Credentials.basic(config.username, config.password)
            var fullUrl = config.url.trim()
            val parsedUrl = fullUrl.toHttpUrlOrNull()
            if (parsedUrl != null && config.port.isNotBlank() && config.port.toIntOrNull() != null) {
                fullUrl = parsedUrl.newBuilder().port(config.port.toInt()).build().toString()
            }
            if (!fullUrl.endsWith("/")) {
                fullUrl += "/"
            }
            if (config.path.isNotBlank()) {
                var pathTrimmed = config.path.trim()
                if (pathTrimmed.startsWith("/")) {
                    pathTrimmed = pathTrimmed.substring(1)
                }
                fullUrl += pathTrimmed
                if (!fullUrl.endsWith("/")) {
                    fullUrl += "/"
                }
            }
            if (!fullUrl.endsWith(".json")) {
                fullUrl += "notes_backup.json"
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(fullUrl)
                .header("Authorization", credential)
                .header("User-Agent", "XiNote/1.0 (Android)")
                .put(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun restoreFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val config = getWebDavConfig()
        if (config.url.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            return@withContext false
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            var fullUrl = config.url.trim()
            val parsedUrl = fullUrl.toHttpUrlOrNull()
            if (parsedUrl != null && config.port.isNotBlank() && config.port.toIntOrNull() != null) {
                fullUrl = parsedUrl.newBuilder().port(config.port.toInt()).build().toString()
            }
            if (!fullUrl.endsWith("/")) {
                fullUrl += "/"
            }
            if (config.path.isNotBlank()) {
                var pathTrimmed = config.path.trim()
                if (pathTrimmed.startsWith("/")) {
                    pathTrimmed = pathTrimmed.substring(1)
                }
                fullUrl += pathTrimmed
                if (!fullUrl.endsWith("/")) {
                    fullUrl += "/"
                }
            }
            if (!fullUrl.endsWith(".json")) {
                fullUrl += "notes_backup.json"
            }

            val request = Request.Builder()
                .url(fullUrl)
                .header("Authorization", credential)
                .header("User-Agent", "XiNote/1.0 (Android)")
                .get()
                .build()

            val jsonString = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                response.body?.string()
            } ?: return@withContext false

            val backupNotes = adapter.fromJson(jsonString) ?: return@withContext false

            for (backupNote in backupNotes) {
                val localNote = noteDao.getNoteById(backupNote.id)
                if (localNote == null) {
                    noteDao.insertNote(backupNote)
                } else if (backupNote.updatedAt > localNote.updatedAt) {
                    noteDao.insertNote(backupNote)
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}

data class WebDavConfig(
    val url: String,
    val port: String = "",
    val username: String,
    val password: String,
    val path: String = ""
)
