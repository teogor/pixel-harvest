package dev.teogor.pixel.harvest.message

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
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
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.util.Date

object ImageDownloader {
    internal fun downloadImages(
        client: DiscordClient,
        event: MessageCreateEvent,
    ) {
        val attachments = event.message.attachments
        val message = event.message.content
        val invoker = event.message.data.mentions()[0]
        val invokerId = invoker.id().asLong()

        if (attachments.isNotEmpty()) {
            val basePath = "${getDownloadsFolderPath()}/${
                client.getBasePathForImages(
                    event = event,
                    haveChannel = false
                )
            }"
            val rootDirectory = basePath.createDirectoryIfNotExists()

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

                // Set the "Date Taken" metadata field
                val imageFile = File(filePath)
                setFileDateTaken(imageFile)
            }
        }
    }

    private fun setFileDateTaken(file: File) {
        try {
            val metadata = ImageMetadataReader.readMetadata(file)
            val directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            directory?.setDate(ExifIFD0Directory.TAG_DATETIME, Date())
            directory?.setDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL, Date())
            directory?.setDate(ExifIFD0Directory.TAG_DATETIME_DIGITIZED, Date())
            Files.setLastModifiedTime(Paths.get(file.absolutePath), FileTime.from(Date().toInstant()))
        } catch (_: Exception) {
        }
    }
}