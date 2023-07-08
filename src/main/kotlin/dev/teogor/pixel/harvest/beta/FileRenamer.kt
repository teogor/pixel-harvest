package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.SvgConverter
import java.io.File

fun main() {
    val baseDownloadPath = "PixelHarvest\\ZeoAI-Automation\\images"
    // val folderPath = "${PathUtils.getDownloadsFolderPath()}\\$baseDownloadPath"
    val folderPath = "E:\\Adobe Stock\\Iconiq Hub\\Images\\set-000001\\split"

    val searchPattern = "l00"
    val replacement = "l_00"

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
