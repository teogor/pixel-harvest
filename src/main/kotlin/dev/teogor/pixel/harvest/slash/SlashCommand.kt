package dev.teogor.pixel.harvest.slash

import dev.kord.common.Color
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
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
import dev.teogor.pixel.harvest.svg.ProgressData
import dev.teogor.pixel.harvest.svg.ProgressListener
import dev.teogor.pixel.harvest.svg.SvgConverter
import dev.teogor.pixel.harvest.utils.Colors
import dev.teogor.pixel.harvest.utils.asBooleanOrDefault
import dev.teogor.pixel.harvest.utils.asStringOrDefault
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.random.Random

sealed class SlashCommand {
    abstract val name: String
    abstract val description: String

    private fun execute(event: GuildChatInputCommandInteractionCreateEvent) {
        runBlocking {
            event.interaction.apply {
                val response = deferEphemeralResponse()
                action(this, response)
            }
        }
        // action(event)
    }

    open suspend fun action(
        interaction: GuildChatInputCommandInteraction,
        response: DeferredEphemeralMessageInteractionResponseBehavior
    ) {

    }

    open fun withOptions(chatInputCreateBuilder: ChatInputCreateBuilder) {

    }

    object GenerateSlashCommand : SlashCommand() {
        override val name: String = "generate"

        override val description: String = "Generate SVGs and scaled images for recent downloads."

        // override val commandRequest: ApplicationCommandRequest
        //     get() = ApplicationCommandRequest.builder()
        //         .name(name)
        //         .description("Generate SVGs and scaled images for recent downloads.")
        //         .addOption(
        //             ApplicationCommandOptionData.builder()
        //                 .name("include-dataset")
        //                 .description("Include dataset generation along with SVGs and scaled images.")
        //                 .type(ApplicationCommandOption.Type.BOOLEAN.value)
        //                 .required(false)
        //                 .build()
        //         )
        //         .build()


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
            super.action(interaction, response)

            val message = kord.rest.interaction.createFollowupMessage(
                applicationId = interaction.applicationId,
                interactionToken = response.token,
                ephemeral = true
            ) {
                content = "Converting to svg in progress..."
            }


            val author = interaction.user
            val username = author.username

            val command = interaction.command
            val includeDataset = command.options["include-dataset"]?.value?.asBooleanOrDefault() ?: false

            val rootPath = "${getDownloadsFolderPath()}\\${
                kord.getBasePathForImages(
                    interaction = interaction,
                    haveChannel = false
                )
            }"
            val inputFolder = File(rootPath)
            val outputFolder = File("${rootPath}\\converter")

            // Create a progress listener or callback
            val progressListener = object : ProgressListener() {
                override suspend fun onProgress(progressData: ProgressData) {
                    // Update the progress of scaled images
                    val progressMessage =
                        "Scaling images: ${(progressData.currentScaledImages)}/${progressData.totalDownloadedImages} complete"
                    println("progressMessage=$progressData")
                    kord.rest.interaction.modifyFollowupMessage(
                        applicationId = interaction.applicationId,
                        interactionToken = response.token,
                        messageId = message.id,
                    ) {
                        content = progressMessage
                    }
                }
            }

            // val progressData = ProgressData(
            //     totalDownloadedImages = 0,
            //     currentScaledImages = 0,
            //     currentSvgConverted = 0
            // )
            // progressListener.onProgress(
            //     progressData.copy(
            //         totalDownloadedImages = inputFolder.countFiles("")
            //     )
            // )

            SvgConverter.Builder(inputFolder, outputFolder)
                .withSvgGenerator(true)
                .withSvgRasterizer(true)
                .withBatchNumber(Random(System.currentTimeMillis()).nextInt(100000000, 999999999))
                .withProgressListener(progressListener) // Set the progress listener
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

        // override val commandRequest: ApplicationCommandRequest
        //     get() = ApplicationCommandRequest.builder()
        //         .name(name)
        //         .description("View and adjust your personal settings.")
        //         .build()
        //
        // override val action: (ChatInputInteractionEvent) -> Unit
        //     get() = { event ->
        //         event.reply("Adjust your settings here.").withEphemeral(true).subscribe()
        //     }
    }

    companion object {
        private val commands: List<SlashCommand> = listOf(
            GenerateSlashCommand,
            GreetSlashCommand,
            InfoSlashCommand,
            SettingsSlashCommand,
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