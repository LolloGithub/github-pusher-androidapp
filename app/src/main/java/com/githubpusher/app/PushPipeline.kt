package com.githubpusher.app

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Unzip, upload files, then delete paths listed in the delete-list file. */
object PushPipeline {

    data class Result(val uploaded: Int, val deleted: Int)

    suspend fun pushZip(
        context: Context,
        uri: Uri,
        config: RepoConfig,
        parsed: RepoUrlParser.OwnerRepo,
        onProgress: (String) -> Unit
    ): Result {
        val entries = withContext(Dispatchers.IO) {
            ZipReader.readAll(context, uri)
        }
        if (entries.isEmpty()) error("Zip contains no files")

        val deleteEntries = entries.filter { DeleteListParser.isDeleteListEntry(it.path) }
        val uploadEntries = entries.filterNot { DeleteListParser.isDeleteListEntry(it.path) }

        val pathsToDelete = deleteEntries
            .flatMap { DeleteListParser.parse(it.bytes.toString(Charsets.UTF_8)) }
            .distinct()

        val uploader = GitHubUploader(
            config.token, parsed.owner, parsed.repo, config.branch
        )
        val prefix = config.pathPrefix.trim('/').let { if (it.isEmpty()) "" else "$it/" }

        // 1) Upload / update files (delete-list file itself is never uploaded)
        uploadEntries.forEachIndexed { index, entry ->
            val remotePath = prefix + entry.path
            onProgress("Uploading ${index + 1}/${uploadEntries.size}: $remotePath")
            withContext(Dispatchers.IO) {
                uploader.putFile(remotePath, entry.bytes, config.defaultCommitMessage)
            }
        }

        // 2) Delete listed paths (repo-root relative; independent of path prefix)
        pathsToDelete.forEachIndexed { index, path ->
            onProgress("Deleting ${index + 1}/${pathsToDelete.size}: $path")
            withContext(Dispatchers.IO) {
                uploader.deleteFile(
                    path,
                    "Delete $path (github-pusher-delete list)"
                )
            }
        }

        return Result(uploaded = uploadEntries.size, deleted = pathsToDelete.size)
    }
}
