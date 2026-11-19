package com.githubpusher.app

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

/** Reads a ZIP from a content/file Uri into path → bytes entries. */
object ZipReader {
    data class Entry(val path: String, val bytes: ByteArray)

    fun readAll(context: Context, uri: Uri): List<Entry> {
        val out = mutableListOf<Entry>()
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open uri: $uri" }
            ZipInputStream(BufferedInputStream(input)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.replace('\\', '/')
                    if (!entry.isDirectory && isAllowed(name)) {
                        val data = zis.readBytes()
                        val path = name.trimStart('/').removePrefix("./")
                        if (path.isNotBlank()) out.add(Entry(path, data))
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        return out
    }

    private fun isAllowed(name: String): Boolean {
        val n = name.replace('\\', '/')
        if (n.startsWith("__MACOSX/")) return false
        if (n.endsWith(".DS_Store")) return false
        return true
    }
}
