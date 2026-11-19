package com.githubpusher.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray

/** Persists RepoConfig list using EncryptedSharedPreferences. */
class ConfigStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "github_zip_pusher_configs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun list(): List<RepoConfig> {
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                add(RepoConfig.fromJson(arr.getJSONObject(i)))
            }
        }
    }

    fun saveAll(configs: List<RepoConfig>) {
        val arr = JSONArray()
        configs.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun upsert(config: RepoConfig) {
        saveAll(list().filterNot { it.id == config.id } + config)
    }

    fun delete(id: String) {
        saveAll(list().filterNot { it.id == id })
    }

    fun get(id: String): RepoConfig? = list().find { it.id == id }

    companion object {
        private const val KEY = "configs_json"
    }
}
