package com.githubpusher.app

/** Parses GitHub repository URLs into owner + name. */
object RepoUrlParser {
    data class OwnerRepo(val owner: String, val repo: String)

    fun parse(input: String): OwnerRepo? {
        var s = input.trim()
        if (s.isEmpty()) return null
        s = s.removeSuffix("/").removeSuffix(".git")
        val markers = listOf("https://github.com/", "http://github.com/", "github.com/")
        for (m in markers) {
            if (s.startsWith(m, ignoreCase = true)) {
                s = s.substring(m.length)
                break
            }
        }
        val parts = s.split("/").filter { it.isNotBlank() }
        if (parts.size < 2) return null
        return OwnerRepo(parts[0], parts[1])
    }
}
