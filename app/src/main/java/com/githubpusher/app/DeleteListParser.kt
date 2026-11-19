package com.githubpusher.app

/**
 * Parses the optional delete-list file inside a zip.
 *
 * Recognized entry names (zip root or any folder):
 * - github-pusher-delete.txt
 * - .github-pusher-delete
 *
 * Format: one repository-relative path per line.
 * Lines starting with # are comments. Blank lines are ignored.
 * Paths are always from the repo root (path prefix is NOT applied).
 */
object DeleteListParser {

    private val FILE_NAMES = setOf(
        "github-pusher-delete.txt",
        ".github-pusher-delete"
    )

    fun isDeleteListEntry(path: String): Boolean {
        val name = path.replace('\\', '/').substringAfterLast('/')
        return name in FILE_NAMES
    }

    fun parse(text: String): List<String> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { it.trimStart('/').replace('\\', '/') }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
    }
}
