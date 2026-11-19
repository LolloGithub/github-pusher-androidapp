package com.githubpusher.app

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Uploads files via GitHub Contents API (one commit per file). */
class GitHubUploader(
    private val token: String,
    private val owner: String,
    private val repo: String,
    private val branch: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun putFile(relativePath: String, content: ByteArray, message: String) {
        val path = relativePath.trimStart('/')
        val sha = getExistingSha(path)
        val body = JSONObject().apply {
            put("message", message)
            put("content", Base64.encodeToString(content, Base64.NO_WRAP))
            put("branch", branch)
            if (sha != null) put("sha", sha)
        }
        val req = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/contents/${enc(path)}")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .put(body.toString().toRequestBody(jsonType))
            .build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw RuntimeException("HTTP ${resp.code} for $path: ${text.take(400)}")
            }
        }
    }

    private fun getExistingSha(path: String): String? {
        val req = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/contents/${enc(path)}?ref=$branch")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 404) return null
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw RuntimeException("HTTP ${resp.code} reading $path: ${text.take(300)}")
            }
            return JSONObject(text).optString("sha", null)
        }
    }

    private fun enc(path: String): String =
        path.split("/").joinToString("/") {
            java.net.URLEncoder.encode(it, "UTF-8").replace("+", "%20")
        }
}
