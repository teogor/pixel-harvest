package dev.teogor.pixel.harvest.slash

import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.teogor.pixel.harvest.BotManager.kord
import dev.teogor.pixel.harvest.database.DatabaseManager.getTotalDownloadCountByDiscordUser
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.message.getBasePathForImages
import dev.teogor.pixel.harvest.svg.ProcessingStep
import dev.teogor.pixel.harvest.svg.ProgressData
import dev.teogor.pixel.harvest.svg.ProgressListener
import dev.teogor.pixel.harvest.svg.SvgConverter
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countDirectories
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import dev.teogor.pixel.harvest.utils.Colors
import dev.teogor.pixel.harvest.utils.asBooleanOrDefault
import dev.teogor.pixel.harvest.utils.asStringOrDefault
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

    object GenerateSlashCommand : SlashCommand() {
        private var isRunning = false

        override val deferredEphemeralResponse: Boolean
            get() = isRunning

        override val name: String = "generate"

        override val description: String = "Generate SVGs and scaled images for recent downloads."

        override fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {
            super.withOptions(chatInputCreateBuilder)

            chatInputCreateBuilder.apply {
                boolean(
                    name = "include-dataset",
                    description = "Include dataset generation along with SVGs and scaled images."
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
            val includeDataset = command.options["include-dataset"]?.value?.asBooleanOrDefault() ?: false

            val baseDownloadPath = kord.getBasePathForImages(
                interaction = interaction,
                haveChannel = false
            )
            val rootPath = "${getDownloadsFolderPath()}\\$baseDownloadPath"
            val inputFolder = File(rootPath)
            val outputFolder = File("${rootPath}\\processed")
            val imagesToProcess = inputFolder.countFiles("")

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
                    val percentage = (currentIndex.toDouble() / (imagesToProcess * 2).toDouble()) * 100
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

                    val processingTitle = "Image Processing Progress: $percentage%"
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
                .withSvgRasterizer(true)
                .withIncludeDataset(includeDataset)
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
                    haveChannel = false
                )
            }"
            println(rootPath)

            val author = interaction.user
            val username = author.username

            val downloadCount = getTotalDownloadCountByDiscordUser(author.id.value.toLong())
            kord.rest.interaction.modifyFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                messageId = message.id,
            ) {
                embeds = mutableListOf(
                    EmbedBuilder().apply {
                        title = "Your Info - $username"
                        description = """
                            **Lifetime Images Downloaded:** `$downloadCount`
            
                            **Auto Download:**  `Active`
                            **Channel Subdirectory:**  `Disabled`
                            **Download Folder Root:**  `${rootPath}`
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