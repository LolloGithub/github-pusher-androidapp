package com.githubpusher.app

import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialize / deserialize [RepoConfig] lists for backup files.
 *
 * Format (version 1):
 * {
 *   "version": 1,
 *   "configs": [ { id, name, repoUrl, branch, token, pathPrefix, defaultCommitMessage }, ... ]
 * }
 *
 * WARNING: the file contains GitHub tokens in plain text. Store it privately.
 */
object ConfigBackup {

    private const val VERSION = 1

    fun toJson(configs: List<RepoConfig>): String {
        val arr = JSONArray()
        configs.forEach { arr.put(it.toJson()) }
        return JSONObject()
            .put("version", VERSION)
            .put("configs", arr)
            .toString(2)
    }

    fun fromJson(text: String): List<RepoConfig> {
        val root = JSONObject(text)
        val arr = when {
            root.has("configs") -> root.getJSONArray("configs")
            // allow a bare JSON array of configs
            text.trimStart().startsWith("[") -> JSONArray(text)
            else -> error("Invalid backup: missing configs array")
        }
        return buildList {
            for (i in 0 until arr.length()) {
                add(RepoConfig.fromJson(arr.getJSONObject(i)))
            }
        }
    }
}
