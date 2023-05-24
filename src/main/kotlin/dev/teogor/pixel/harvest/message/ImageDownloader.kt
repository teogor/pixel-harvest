package dev.teogor.pixel.harvest.message

import dev.teogor.pixel.harvest.database.DatabaseManager.addDownload
import dev.teogor.pixel.harvest.discord.PathUtils.getBasePathForImages
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import dev.teogor.pixel.harvest.utils.createDirectoryIfNotExists
import dev.teogor.pixel.harvest.utils.extractFilename
import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageDownloader {
    internal fun downloadImages(
        client: DiscordClient,
        event: MessageCreateEvent,
    ) {
        val attachments = event.message.attachments
        val message = event.message.content
        val invoker = event.message.data.mentions()[0]
        val invokerId = invoker.id().asLong()

        var matches = 0
        event.message.components.forEach { component ->
            component.data.components().get().forEach { element ->
                if (!element.label().isAbsent) {
                    val label = element.label().get()
                    val pattern = Regex("^[UV][1-4]$")
                    if (pattern.matches(label)) {
                        matches++
                    }
                }
            }
        }
        if (matches < 2) {
            if (attachments.isNotEmpty()) {
                val basePath = "${getDownloadsFolderPath()}/${
                    client.getBasePathForImages(
                        event = event,
                        haveChannel = false
                    )
                }"
                val rootDirectory = basePath.createDirectoryIfNotExists()
                rootDirectory.mkdirs()

                for (attachment in attachments) {
                    val imageUrl = attachment.url
                    val extension = File(attachment.filename).extension
                    val extractFileName = message.extractFilename
                    val index = rootDirectory.countFiles(extractFileName)
                    val fileName = if (index == 0) {
                        extractFileName
                    } else {
                        "$extractFileName (${index.toString().padStart(4, '0')})"
                    }
                    val filePath = "${basePath}${fileName}.$extension"
                    addDownload(
                        discordId = invokerId,
                        url = imageUrl,
                    )

                    // Download the image
                    val url = URL(imageUrl)
                    val connection = url.openConnection()
                    connection.connect()

                    // Save the image to a file
                    val inputStream = BufferedInputStream(connection.getInputStream())
                    val outputStream = FileOutputStream(filePath)

                    val buffer = ByteArray(4096)
                    var bytesRead = inputStream.read(buffer)
                    while (bytesRead != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRead = inputStream.read(buffer)
                    }

                    outputStream.close()
                    inputStream.close()
                }
            }
        }
    }
}