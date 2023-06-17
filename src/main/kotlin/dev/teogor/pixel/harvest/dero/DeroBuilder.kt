package dev.teogor.pixel.harvest.dero

import dev.teogor.pixel.harvest.discord.PathUtils
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

fun main() {
    val folderPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images"

    println(folderPath)
    DeroBuilder().apply {
        val batchFolder = copyFilesToNewFolder(folderPath)
        println("Batch Folder Ready at ${batchFolder.absolutePath}")
    }
}

class DeroBuilder {
    fun copyFilesToNewFolder(folderPath: String) : File {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM_dd_yyyy_HHmmss"))
        val randomNumber = Random.nextInt(1000, 10000)
        val newFolderName = "batch_job_$timestamp" + "_$randomNumber"
        val newFolderPath = "$folderPath/$newFolderName"
        val newFolder = File(newFolderPath)
        newFolder.mkdirs()

        val sourceFolder = File(folderPath)
        val files = sourceFolder.listFiles()

        files?.forEach { file ->
            if(file.isFile) {
                val destinationFile = File("$newFolderPath/${file.name}")
                file.renameTo(destinationFile)
            }
        }

        return newFolder
    }
}
