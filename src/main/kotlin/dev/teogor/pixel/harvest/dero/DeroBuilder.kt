package dev.teogor.pixel.harvest.dero

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

fun main() {
    val folderPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images"

    println(folderPath)
    runBlocking {
        val deroBuilder = DeroBuilder()
        val batchFolder = deroBuilder.copyFilesToNewFolder(folderPath)
        val imagesToProcess = batchFolder.countFiles("")
        println("Batch Folder Ready at ${batchFolder.absolutePath}[files:$imagesToProcess]")
    }
}

class DeroBuilder {
    suspend fun copyFilesToNewFolder(folderPath: String) : File = withContext(Dispatchers.IO) {
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
                // todo replace to this once debug is done
                //  file.renameTo(destinationFile)
                file.copyTo(destinationFile)
            }
        }

        newFolder
    }
}
