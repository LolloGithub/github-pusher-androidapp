package com.githubpusher.app

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Unzip + upload each entry using GitHubUploader. */
object PushPipeline {
    suspend fun pushZip(
        context: Context,
        uri: Uri,
        config: RepoConfig,
        parsed: RepoUrlParser.OwnerRepo,
        onProgress: (String) -> Unit
    ): Int {
        val entries = withContext(Dispatchers.IO) {
            ZipReader.readAll(context, uri)
        }
        if (entries.isEmpty()) error("Zip contains no files")
        val uploader = GitHubUploader(config.token, parsed.owner, parsed.repo, config.branch)
        val prefix = config.pathPrefix.trim('/').let { if (it.isEmpty()) "" else "$it/" }
        entries.forEachIndexed { index, entry ->
            val remotePath = prefix + entry.path
            onProgress("Uploading ${index + 1}/${entries.size}: $remotePath")
            withContext(Dispatchers.IO) {
                uploader.putFile(remotePath, entry.bytes, config.defaultCommitMessage)
            }
        }
        return entries.size
    }
}
