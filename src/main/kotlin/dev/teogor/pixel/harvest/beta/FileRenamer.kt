package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.SvgConverter
import java.io.File

fun main() {
    val baseDownloadPath = "PixelHarvest\\ZeoAI-Automation\\images"
    val folderPath = "${PathUtils.getDownloadsFolderPath()}\\$baseDownloadPath"

    val searchPattern = "Create a patterned image featuring "
    val replacement = ""

    val folder = File(folderPath)

    if (folder.exists() && folder.isDirectory) {
        val files = folder.listFiles()
        files?.forEach { file ->
            val originalName = file.name
            val newName = originalName.replace(searchPattern, replacement)
            if (newName != originalName) {
                val renamedFile = File(folder, newName)
                if (file.renameTo(renamedFile)) {
                    println("Renamed file: $originalName -> $newName")
                } else {
                    println("Failed to rename file: $originalName")
                }
            }
        }
    } else {
        println("Invalid folder path: $folderPath")
    }
}
