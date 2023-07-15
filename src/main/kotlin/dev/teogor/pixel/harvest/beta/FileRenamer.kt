package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.SvgConverter
import java.io.File

class FileRenamer private constructor(
    val directory: File,
    val searchPattern: String,
    val replacement: String,
) {

    companion object {
        fun forDirectory(
            directory: File,
            searchPattern: String,
            replacement: String,
        ): FileRenamer {
            return FileRenamer(
                directory = directory,
                searchPattern = searchPattern,
                replacement = replacement,
            ).apply {
                renameWithinDirectory()
            }
        }
    }

    private fun renameWithinDirectory() {
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            files?.forEach { file ->
                val originalName = file.name
                val newName = originalName.replace(searchPattern, replacement)
                if (newName != originalName) {
                    val renamedFile = File(directory, newName)
                    if (file.renameTo(renamedFile)) {
                        println("Renamed file: $originalName -> $newName")
                    } else {
                        println("Failed to rename file: $originalName")
                    }
                }
            }
        } else {
            println("Invalid folder path: ${directory.absolutePath}")
        }
    }
}

fun main() {
    // sof::customizable
    val directoryPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images\\photo-ai"
    val searchPattern = "Generate "
    val replacement = ""
    // eof::customizable

    FileRenamer.forDirectory(
        directory = File(directoryPath),
        searchPattern = searchPattern,
        replacement = replacement,
    )
}
