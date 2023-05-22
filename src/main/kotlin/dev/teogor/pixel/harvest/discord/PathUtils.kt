package dev.teogor.pixel.harvest.discord

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import java.io.File
import java.nio.file.FileSystems

object PathUtils {

    /**
     * Retrieves the base path for images based on the server and channel information.
     *
     * @param event The message create event.
     * @return The base path for images.
     */
    fun DiscordClient.getBasePathForImages(
        event: MessageCreateEvent,
        haveChannel: Boolean = true,
    ): String {
        val serverName = (event.guild.block()?.name ?: "Unknown Server").replace(" ", "-")
        val channelName = getChannelNameById(event.message.channelId).replace(" ", "-")
        val basePath = if (haveChannel) {
            "PixelHarvest/$serverName/images/$channelName/"
        } else {
            "PixelHarvest/$serverName/images/"
        }
        return basePath
    }

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