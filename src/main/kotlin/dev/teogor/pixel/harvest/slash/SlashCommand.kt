package dev.teogor.pixel.harvest.slash

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.channel
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.json.request.BulkDeleteRequest
import dev.teogor.pixel.harvest.BotManager.kord
import dev.teogor.pixel.harvest.beta.ColorPairGenerator
import dev.teogor.pixel.harvest.database.DatabaseManager.getTotalDownloadCountByDiscordUser
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.message.ImageDownloader
import dev.teogor.pixel.harvest.message.getBasePathForImages
import dev.teogor.pixel.harvest.svg.ProcessingStep
import dev.teogor.pixel.harvest.svg.ProgressData
import dev.teogor.pixel.harvest.svg.ProgressListener
import dev.teogor.pixel.harvest.svg.SvgConverter
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countDirectories
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import dev.teogor.pixel.harvest.utils.Colors
import dev.teogor.pixel.harvest.utils.asBooleanOrDefault
import dev.teogor.pixel.harvest.utils.asStringDefault
import dev.teogor.pixel.harvest.utils.asStringOrDefault
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.File

sealed class SlashCommand {
    abstract val name: String
    abstract val description: String

    open val deferredEphemeralResponse: Boolean
        get() = true

    private fun execute(event: GuildChatInputCommandInteractionCreateEvent) {
        runBlocking {
            event.interaction.apply {
                if (deferredEphemeralResponse) {
                    val response = deferEphemeralResponse()
                    action(this, response)
                } else {
                    val response = deferPublicResponse()
                    action(this, response)
                }
            }
        }
    }

    open suspend fun action(
        interaction: GuildChatInputCommandInteraction,
        response: DeferredPublicMessageInteractionResponseBehavior
    ) {

    }

    open suspend fun action(
        interaction: GuildChatInputCommandInteraction,
        response: DeferredEphemeralMessageInteractionResponseBehavior
    ) {

    }

    open fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {

    }

    object ClearChannelSlashCommand : SlashCommand() {
        override val name: String = "clear"

        override val description: String = "Clear all messages from a channel"

        override fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {
            super.withOptions(chatInputCreateBuilder)

            chatInputCreateBuilder.apply {
                channel(name = "channel", description = "The target channel") {
                    required = true
                }
            }
        }

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            super.action(interaction, response)

            val command = interaction.command
            val channelId = command.options["channel"].asStringDefault("")

            if (channelId.isEmpty()) {
                return
            }

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Greetings, $name!"
            }
            val channel = kord.getChannelOf<MessageChannel>(Snowflake(channelId.toLong()))
            channel?.let {
                val lastMessageId = it.lastMessageId ?: return@let
                val messages = it.getMessagesBefore(lastMessageId).toList().map { message -> message.id }
                val bulkDeleteRequest = BulkDeleteRequest(messages)
                kord.rest.channel.bulkDelete(Snowflake(channelId.toLong()), bulkDeleteRequest)
            }
        }
    }

    object ColorPromptSlashCommand : SlashCommand() {
        override val name: String = "color-pair"

        override val description: String = "Generates a random pair of colors."

        override fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {
            chatInputCreateBuilder.apply {
                string(
                    name = "format",
                    description = "Format of color pair. Example: `\$color1 to \$color2`, `\$c1, \$c2`"
                ) {
                    required = true
                }
                string(
                    name = "prompt",
                    description = "The pair to be applied to. Example: `background gradient \$colors`"
                ) {
                    required = false
                }
            }
        }

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            val command = interaction.command
            val formatOption = command.options["format"].asStringDefault("\$c1 \$c2")
            val promptOption = command.options["prompt"].asStringDefault("\$colors")

            val pairCount = 10
            val imaginePrompt = "/imagine prompt:"
            val colorPairGenerator = ColorPairGenerator()
            val pairs = colorPairGenerator.generateRandomColorPairs(
                pairCount = pairCount,
                format = formatOption,
                allowReversed = false,
                includeAdditionalColors = false,
            )
            val midjourneySyntax = pairs.joinToString(separator = ",", prefix = "{", postfix = "}")
            val endPrompt = "$imaginePrompt${promptOption.replace("\$colors", midjourneySyntax)}"

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = """
                    ```txt
                    $endPrompt
                    ```
                    """.trimIndent()
            }
        }
    }

    /**
     * todo: add delete button since this is not ephemeral
     */
    object GenerateSlashCommand : SlashCommand() {
        private var isRunning = false

        override val deferredEphemeralResponse: Boolean
            get() = isRunning

        override val name: String = "generate"

        override val description: String = "Generate SVGs and scaled images for recent downloads."

        private const val optionIncludeDataset = "include-dataset"
        private const val optionScalingEnabled = "scaling-enabled"
        private const val optionSplitEnabled = "split-enabled"

        override fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {
            super.withOptions(chatInputCreateBuilder)

            chatInputCreateBuilder.apply {
                boolean(
                    name = optionIncludeDataset,
                    description = "Include dataset generation along with SVGs and scaled images."
                ) {
                    required = false
                    default = false
                }
                boolean(
                    name = optionScalingEnabled,
                    description = "Enable scaling by 6x for generated SVGs images for more versatile output."
                ) {
                    required = false
                    default = false
                }
                boolean(
                    name = optionSplitEnabled,
                    description = "Split generated images into SVG and scaled versions, alternating by index."
                ) {
                    required = false
                    default = false
                }
            }
        }

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            var message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = isRunning
            ) {
                content = if (!isRunning) "Process downloaded images. (Waiting to start)" else "Job already running."
            }
            if (isRunning) {
                return
            }
        }

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredPublicMessageInteractionResponseBehavior
        ) {
            var message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = isRunning
            ) {
                content = if (!isRunning) "Process downloaded images. (Waiting to start)" else "Job already running."
            }
            if (isRunning) {
                return
            }
            isRunning = true

            val author = interaction.user
            val username = author.username

            val command = interaction.command
            val includeDataset = command.options[optionIncludeDataset]?.value?.asBooleanOrDefault() ?: false
            val scalingEnabled = command.options[optionScalingEnabled]?.value?.asBooleanOrDefault() ?: false
            val splitEnabled = command.options[optionSplitEnabled]?.value?.asBooleanOrDefault() ?: false

            val baseDownloadPath = kord.getBasePathForImages(
                interaction = interaction,
                haveChannel = false,
            )
            val rootPath = "${getDownloadsFolderPath()}\\$baseDownloadPath"
            val inputFolder = File(rootPath)
            val outputFolder = File("${rootPath}\\processed")
            val imagesToProcess = inputFolder.countFiles("")

            if (imagesToProcess == 0) {
                message = kord.rest.interaction.modifyFollowupMessage(
                    applicationId = interaction.applicationId,
                    interactionToken = response.token,
                    messageId = message.id,
                ) {
                    content = ""
                    embeds = mutableListOf(
                        EmbedBuilder().apply {
                            description = """
                                There are no images to be processed.
                            """.trimIndent()
                            color = Colors.RED
                        }
                    )
                }
                return
            }

            val progressData = ProgressData(
                currentIndex = 0,
                processingStep = ProcessingStep.PARSING,
            )
            val nextFolder = outputFolder.countDirectories("set") + 1
            var totalTimeElapsed = 0L
            var previousTime = System.currentTimeMillis()
            var progressCalls = 0
            val progressListener = object : ProgressListener() {
                override suspend fun onProgress(progressData: ProgressData) {
                    val currentTime = System.currentTimeMillis()
                    val timeElapsed = currentTime - previousTime
                    totalTimeElapsed += timeElapsed
                    progressCalls++
                    previousTime = currentTime

                    val averageTime = totalTimeElapsed / progressCalls

                    val currentIndex = when (progressData.processingStep) {
                        ProcessingStep.PARSING -> 0
                        ProcessingStep.GENERATING_SVG -> progressData.currentIndex
                        ProcessingStep.SCALING_SVG -> imagesToProcess + progressData.currentIndex
                        ProcessingStep.DONE -> imagesToProcess * 2
                    }
                    val progress = (currentIndex.toDouble() / (imagesToProcess * 2).toDouble()) * 100
                    val formattedProgress = String.format("%.2f", progress)
                    val remainingIterations = imagesToProcess * 2 - currentIndex
                    val estimatedRemainingTime = averageTime * remainingIterations

                    val estimatedRemainingTimeText = if (progressData.processingStep == ProcessingStep.DONE) {
                        "Done"
                    } else if (estimatedRemainingTime > 0) {
                        val remainingTimeSeconds = estimatedRemainingTime / 1000
                        val remainingTimeMinutes = remainingTimeSeconds / 60
                        val remainingTimeHours = remainingTimeMinutes / 60

                        val hoursText = if (remainingTimeHours > 0) "${remainingTimeHours}h " else ""
                        val minutesText = if (remainingTimeMinutes % 60 > 0) "${remainingTimeMinutes % 60}m " else ""
                        val secondsText = if (remainingTimeSeconds % 60 > 0) "${remainingTimeSeconds % 60}s" else ""

                        "Estimated Remaining Time: $hoursText$minutesText$secondsText"
                    } else {
                        "Estimated Remaining Time: Calculating..."
                    }

                    val stepDescription = when (progressData.processingStep) {
                        ProcessingStep.PARSING -> "Parsing..."
                        ProcessingStep.GENERATING_SVG -> "Generating SVGs: ${progressData.currentIndex} out of $imagesToProcess"
                        ProcessingStep.SCALING_SVG -> "Scaling SVGs: ${progressData.currentIndex} out of $imagesToProcess"
                        ProcessingStep.DONE -> "DONE"
                    }

                    val processingTitle = "Image Processing Progress: $formattedProgress%"
                    val imagesToProcessLabel = "Total Images to Process:"
                    val currentStepLabel = "Current Step"
                    val downloadLocationLabel = "Download Location (Output Folder)"
                    val lineColor = if (progressData.processingStep == ProcessingStep.DONE) {
                        Colors.LIME_GREEN
                    } else {
                        Colors.CORNFLOWER_BLUE
                    }

                    if (progressData.processingStep == ProcessingStep.DONE) {
                        isRunning = false
                    }

                    message = kord.rest.interaction.modifyFollowupMessage(
                        applicationId = interaction.applicationId,
                        interactionToken = response.token,
                        messageId = message.id,
                    ) {
                        content = ""
                        embeds = mutableListOf(
                            EmbedBuilder().apply {
                                title = processingTitle
                                description = """
                        **$imagesToProcessLabel** `${imagesToProcess.toString().padStart(3, '0')}`
                        
                        **$currentStepLabel**
                        $stepDescription
                        
                        $estimatedRemainingTimeText
                        
                        **$downloadLocationLabel:**
                        `Downloads\${baseDownloadPath}processed\set-${nextFolder.toString().padStart(6, '0')}`
                    """.trimIndent()
                                color = lineColor
                            }
                        )
                    }
                }
            }

            progressListener.onProgress(progressData)

            SvgConverter.Builder(inputFolder, outputFolder)
                .withSvgGenerator(true)
                .withSvgRasterizer(scalingEnabled || splitEnabled)
                .withIncludeDataset(includeDataset)
                .withSplitEnabled(splitEnabled)
                .withBatchNumber(nextFolder)
                .withProgressListener(progressListener)
                .build()

        }
    }

    object GreetSlashCommand : SlashCommand() {
        override val name: String = "greet"

        override val description: String = "Greets you"

        override fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {
            super.withOptions(chatInputCreateBuilder)

            chatInputCreateBuilder.apply {
                string(name = "name", description = "Your Name") {
                    required = true
                }
            }
        }

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            super.action(interaction, response)

            val command = interaction.command
            val name = command.options["name"]?.value?.asStringOrDefault()

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Greetings, $name!"
            }
        }
    }

    object InfoSlashCommand : SlashCommand() {
        override val name: String = "info"

        override val description: String = "View information about your profile."

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            super.action(interaction, response)

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Retrieving Data..."
            }

            val rootPath = "Downloads\\${
                kord.getBasePathForImages(
                    interaction = interaction,
                    haveChannel = false,
                )
            }"

            val author = interaction.user
            val username = author.username

            val downloadCount = getTotalDownloadCountByDiscordUser(author.id.value.toLong())
            var currentDownloadQueue = 0
            ImageDownloader.currentQueueSize { size ->
                // Handle the updated queue size here
                currentDownloadQueue = size

                runBlocking {
                    sendInfoMessage(
                        interaction = interaction,
                        response = response,
                        message = message,
                        username = username,
                        downloadCount = downloadCount,
                        rootPath = rootPath,
                        currentDownloadQueue = currentDownloadQueue
                    )
                }
            }
            sendInfoMessage(
                interaction = interaction,
                response = response,
                message = message,
                username = username,
                downloadCount = downloadCount,
                rootPath = rootPath,
                currentDownloadQueue = currentDownloadQueue
            )
        }

        private suspend fun sendInfoMessage(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior,
            message: DiscordMessage,
            username: String,
            downloadCount: Long,
            rootPath: String,
            currentDownloadQueue: Int
        ) {
            kord.rest.interaction.modifyFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                messageId = message.id,
            ) {
                content = ""
                embeds = mutableListOf(
                    EmbedBuilder().apply {
                        title = "Your Info - $username"
                        description = """
                    **Lifetime Images Downloaded:** `$downloadCount`
    
                    **Auto Download:**  `Active`
                    **Channel Subdirectory:**  `Disabled`
                    **Download Folder Root:**  `${rootPath}`
                    
                    **Queued Jobs (download)**: $currentDownloadQueue
                    **Running Jobs**: None
                """.trimIndent()
                        color = Colors.GREEN
                    }
                )
            }
        }
    }

    object SettingsSlashCommand : SlashCommand() {
        override val name: String = "settings"

        override val description: String = "View and adjust your personal settings."

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            super.action(interaction, response)

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Adjust your settings here."
            }
        }
    }

    object PingCommand : SlashCommand() {
        override val name: String = "ping"

        override val description: String = "Pong! \uD83C\uDFD3"

        override suspend fun action(
            interaction: GuildChatInputCommandInteraction,
            response: DeferredEphemeralMessageInteractionResponseBehavior
        ) {
            kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Pong! \uD83C\uDFD3 `${kord.gateway.averagePing}`"
            }
        }
    }

    companion object {
        private val commands: List<SlashCommand> = listOf(
            ClearChannelSlashCommand,
            ColorPromptSlashCommand,
            GenerateSlashCommand,
            GreetSlashCommand,
            InfoSlashCommand,
            SettingsSlashCommand,
            PingCommand,
        )

        fun forEachCommand(action: (SlashCommand) -> Unit) {
            commands.forEach {
                action(it)
            }
        }

        fun executeCommand(
            event: GuildChatInputCommandInteractionCreateEvent,
        ) {
            commands.filter {
                it.name == event.interaction.invokedCommandName
            }.forEach {
                it.execute(event)
            }
        }
    }
}