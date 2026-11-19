package com.githubpusher.app

import org.json.JSONObject
import java.util.UUID

/** One saved target repository configuration. Tokens stay on-device only. */
data class RepoConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val repoUrl: String,
    val branch: String = "main",
    val token: String,
    val pathPrefix: String = "",
    val defaultCommitMessage: String = "Update files from zip"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("repoUrl", repoUrl)
        put("branch", branch)
        put("token", token)
        put("pathPrefix", pathPrefix)
        put("defaultCommitMessage", defaultCommitMessage)
    }

    companion object {
        fun fromJson(o: JSONObject): RepoConfig = RepoConfig(
            id = o.getString("id"),
            name = o.getString("name"),
            repoUrl = o.getString("repoUrl"),
            branch = o.optString("branch", "main"),
            token = o.getString("token"),
            pathPrefix = o.optString("pathPrefix", ""),
            defaultCommitMessage = o.optString("defaultCommitMessage", "Update files from zip")
        )
    }
}
