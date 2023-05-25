package dev.teogor.pixel.harvest.message

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.create.embed
import dev.teogor.pixel.harvest.BotManager
import dev.teogor.pixel.harvest.database.DatabaseManager.addDownload
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.models.Bot
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import dev.teogor.pixel.harvest.utils.Emoji
import dev.teogor.pixel.harvest.utils.createDirectoryIfNotExists
import dev.teogor.pixel.harvest.utils.extractPromptName
import dev.teogor.pixel.harvest.utils.getRandomColor
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageDownloader {
    private val downloadQueue: MutableList<Message> = mutableListOf()
    private var isDownloading: Boolean = false

    fun addToQueue(message: Message) {
        message.addReaction(Emoji.FileDownloadQueue)
        if (!downloadQueue.contains(message)) {
            downloadQueue.add(message)
        }

        if (!isDownloading) {
            startDownloading()
        }
    }

    private fun startDownloading() {
        isDownloading = true

        while (downloadQueue.isNotEmpty()) {
            val message = downloadQueue.removeAt(0)
            downloadImages(message)
        }

        isDownloading = false
    }

    private fun downloadImages(
        message: Message,
    ) {
        message.removeAllReaction(Emoji.FileDownloadQueue)
        val attachments = message.attachments
        val content = message.content
        val invokerId = message.data.mentions[0].value.toLong()

        var matches = 0
        val componentsOptional = message.data.components
        componentsOptional.value?.let {
            it.forEach { component ->
                component.components.value?.forEach { element ->
                    element.label.value?.let { label ->
                        val pattern = Regex("^[UV][1-4]$")
                        if (pattern.matches(label)) {
                            matches++
                        }
                    }
                }
            }
        }

        if (matches < 2) {
            message.addReaction(Emoji.FileDownloading)
            if (attachments.isNotEmpty()) {
                val basePath = "${getDownloadsFolderPath()}/${
                    BotManager.kord.getBasePathForImages(
                        message = message,
                        haveChannel = false
                    )
                }"

                val rootDirectory = basePath.createDirectoryIfNotExists()
                rootDirectory.mkdirs()

                for (attachment in attachments) {
                    val imageUrl = attachment.url
                    val extension = File(attachment.filename).extension
                    val extractFileName = content.extractPromptName
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

                    message.removeReaction(Emoji.FileDownloading)
                    message.addReaction(Emoji.FileDownloaded)
                }
            }
        } else {
            // todo mention @author in a new thread so he does not forget about it
            runBlocking {
                val channel = BotManager.kord.getChannelOf<TextChannel>(MessageDiscordModule.ImagineChannel.id)
                channel?.let {
                    val messageLink =
                        "https://discord.com/channels/${channel.guildId.value}/${message.channelId.value}/${message.id.value}"
                    message.mentionedUsers.collect {
                        val user = BotManager.kord.getUser(Snowflake(it.id.value))
                        user?.let {
                            val extractPromptName = content.extractPromptName
                            val extractPromptArgs = content.extractPromptName
                            val textVariants = listOf(
                                "Imagine prompt created with `\\imagine` - link to the message: $messageLink",
                                "Generated prompt using `\\imagine` - message URL: $messageLink",
                                "New prompt generated via `\\imagine` - see it at $messageLink",
                                "Prompt created with `\\imagine` - check it out: $messageLink",
                                "Imagine-generated prompt with `\\imagine` - message link: $messageLink"
                            )
                            BotManager.kord.rest.channel.createMessage(
                                channelId = MessageDiscordModule.ImagineChannel.id,
                            ) {
                                this.embed {
                                    description = """
                                                ${textVariants.random()}
                                                **Prompt** `${extractPromptName}`
                                                **Params** `${extractPromptArgs}`
                                                **Author** ${user.mention} | **AI Generator** ${message.author?.mention ?: "Unknown"}
                                            """.trimIndent()
                                    color = getRandomColor()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Message.addReaction(emoji: Emoji) {
    runBlocking {
        addReaction(emoji.asReactionEmoji())
    }
}

fun Message.removeAllReaction(emoji: Emoji) {
    runBlocking {
        deleteReaction(emoji.asReactionEmoji())
    }
}

fun Message.removeReaction(emoji: Emoji) {
    runBlocking {
        emoji.asReactionEmoji().let {
            kord.rest.channel.deleteReaction(
                channelId = channelId,
                messageId = id,
                userId = Snowflake(Bot.PixelHarvestBot.id),
                emoji = it.urlFormat
            )
        }
    }
}

fun Kord.getBasePathForImages(
    message: Message,
    haveChannel: Boolean = true,
): String {
    var basePath: String
    runBlocking {
        val guild = message.getGuildOrNull()?.data
        val channelId = message.channelId
        val channel = getChannelOf<Channel>(channelId)
        val serverName = guild?.name?.replace(" ", "-") ?: "Unknown Server"
        val channelName = channel?.data?.name?.value?.replace(" ", "-") ?: "Unknown Channel"

        basePath = if (haveChannel) {
            "PixelHarvest/$serverName/images/$channelName/"
        } else {
            "PixelHarvest/$serverName/images/"
        }
    }
    return basePath.replace("/", "\\")
}

fun Kord.getBasePathForImages(
    interaction: GuildChatInputCommandInteraction,
    haveChannel: Boolean = true,
): String {
    var basePath: String
    runBlocking {
        val guild = interaction.getGuildOrNull()?.data
        val channelId = interaction.channelId
        val channel = getChannelOf<Channel>(channelId)
        val serverName = guild?.name?.replace(" ", "-") ?: "Unknown Server"
        val channelName = channel?.data?.name?.value?.replace(" ", "-") ?: "Unknown Channel"

        basePath = if (haveChannel) {
            "PixelHarvest/$serverName/images/$channelName/"
        } else {
            "PixelHarvest/$serverName/images/"
        }
    }
    return basePath.replace("/", "\\")
}