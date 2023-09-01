package dev.teogor.pixel.harvest.message

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Attachment
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
import dev.teogor.pixel.harvest.utils.ParamsData.Companion.formatParamsData
import dev.teogor.pixel.harvest.utils.createDirectoryIfNotExists
import dev.teogor.pixel.harvest.utils.extractPromptName
import dev.teogor.pixel.harvest.utils.extractText
import dev.teogor.pixel.harvest.utils.getParams
import dev.teogor.pixel.harvest.utils.getRandomColor
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class MessageData(
    val message: Message,
    val attachment: Attachment,
    val fileName: String,
    val filePath: String,
)

object ImageDownloader2 {
    private val parseQueue: MutableList<Message> = mutableListOf()
    private val downloadQueue: MutableList<MessageData> = mutableListOf()
    private var isParsing: Boolean = false
    private var isDownloading: Boolean = false
    private var queueSizeCallback: ((Int) -> Unit)? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val downloadDispatcher = newFixedThreadPoolContext(
        10,
        "ImageDownloader::Download"
    )

    @OptIn(DelicateCoroutinesApi::class)
    private val parseDispatcher = newFixedThreadPoolContext(
        10,
        "ImageDownloader::Parse"
    )

    fun addToQueue(message: Message) {
        message.addReaction(Emoji.FileDownloadQueue)
        if (!parseQueue.contains(message)) {
            parseQueue.add(message)
            notifyQueueSizeChanged()
        }

        if (!isParsing) {
            runBlocking {
                parseQueue()
            }
        }
    }

    private suspend fun checkDownload() {
        if (!isDownloading) {
            downloadQueue()
        }
    }

    fun currentQueueSize(callback: (Int) -> Unit) {
        queueSizeCallback = callback
        notifyQueueSizeChanged()
    }

    private fun notifyQueueSizeChanged() {
        queueSizeCallback?.invoke(downloadQueue.size)
    }

    private suspend fun parseQueue() {
        withContext(Dispatchers.IO) {
            isParsing = true
            parseQueue.map { message ->
                launch(parseDispatcher) {
                    parseMessage(message)
                }
            }
        }
        isParsing = false
    }

    private suspend fun downloadQueue() {
        withContext(Dispatchers.IO) {
            isDownloading = true
            downloadQueue.map { messageData ->
                launch(downloadDispatcher) {
                    downloadImage(messageData)
                }
            }
        }
        isDownloading = false
    }

    private suspend fun parseMessage(
        message: Message,
    ) {
        val attachments = message.attachments
        val content = message.content

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

        message.removeAllReaction(Emoji.FileDownloadQueue)
        if (matches < 2) {

            message.addReaction(Emoji.FileDownloading)
            if (attachments.isNotEmpty()) {
                val basePath = "${getDownloadsFolderPath()}/${
                    BotManager.kord.getBasePathForImages(
                        message = message,
                        haveChannel = false,
                    )
                }"
                val rootDirectory = basePath.createDirectoryIfNotExists()
                rootDirectory.mkdirs()

                for (attachment in attachments) {
                    val extractFileName = content.extractPromptName
                    val extension = File(attachment.filename).extension
                    val index = rootDirectory.countFiles(extractFileName)
                    val fileName = if (index == 0) {
                        extractFileName
                    } else {
                        "$extractFileName (${index.toString().padStart(4, '0')})"
                    }
                    val filePath = "${basePath}${fileName}.$extension"

                    val messageData = MessageData(
                        message = message,
                        attachment = attachment,
                        fileName = fileName,
                        filePath = filePath,
                    )
                    if (!downloadQueue.contains(messageData)) {
                        downloadQueue.add(messageData)
                        checkDownload()
                    }
                }
            }
        } else {
            notifyNewPrompt(
                message,
                content
            )
        }
    }

    private suspend fun downloadImage(
        messageData: MessageData
    ) {
        val (message, attachment, fileName, filePath) = messageData
        val invokerId = message.data.mentions[0].value.toLong()
        addDownload(
            discordId = invokerId,
            url = attachment.url,
        )

        // Download the image
        val url = URL(attachment.url)
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

    private suspend fun notifyNewPrompt(message: Message, content: String) {
        // todo mention @author in a new thread so he does not forget about it
        runBlocking {
            val channel = BotManager.kord.getChannelOf<TextChannel>(MessageDiscordModule.ImagineChannel.id)
            channel?.let {
                val messageLink =
                    "https://discord.com/channels/${channel.guildId.value}/${message.channelId.value}/${message.id.value}"
                message.mentionedUsers.collect {
                    val user = BotManager.kord.getUser(Snowflake(it.id.value))
                    user?.let {
                        val extractPromptName = content.extractText()
                        val extractPromptArgs = content.getParams()
                            .formatParamsData(prefix = "--")
                            .replace("*", "")
                        val messageLinkVariants = listOf(
                            "link to the message: $messageLink",
                            "message URL: $messageLink",
                            "see it at $messageLink",
                            "check it out: $messageLink",
                            "message link: $messageLink",
                            "message can be found at $messageLink",
                            "explore the message here: $messageLink",
                            "discover the message at $messageLink",
                            "take a look at the message: $messageLink",
                            "access the message via $messageLink"
                        )
                        val titleVariants = listOf(
                            "New Prompt Created with Imagine Command",
                            "Generated Prompt using Imagine Command",
                            "Imagine-Generated Prompt",
                            "Prompt Created with Imagine Command",
                            "Imagine Prompt Generated",
                            "Fresh Prompt Generated via Imagine Command",
                            "Imagine Command Unveils a Brand-New Prompt",
                            "Imagine Command Creates a Spark",
                        )
                        BotManager.kord.rest.channel.createMessage(
                            channelId = MessageDiscordModule.ImagineChannel.id,
                        ) {
                            this.embed {
                                title = titleVariants.random()
                                description = """
                                                ${messageLinkVariants.random()}
                                                
                                                **Prompt:** `${extractPromptName}`
                                                **Params:** `${extractPromptArgs}`
                                                
                                                **Author** ${user.mention} | **AI Generator** ${message.author?.mention ?: "Unknown"}
                                            """.trimIndent()
                                color = getRandomColor()
                                timestamp = Clock.System.now()
                            }
                        }
                    }
                }
            }
        }
    }
}