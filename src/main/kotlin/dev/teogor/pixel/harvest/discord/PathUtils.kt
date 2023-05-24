package dev.teogor.pixel.harvest.discord

import java.nio.file.FileSystems

object PathUtils {
    fun getDownloadsFolderPath(): String? {
        val fileSystem = FileSystems.getDefault()
        val userHome = System.getProperty("user.home")
        val downloadsDir = fileSystem.getPath(userHome, "Downloads")

        return if (downloadsDir.toFile().exists()) {
            downloadsDir.toAbsolutePath().toString()
        } else {
            null
        }
    }
}