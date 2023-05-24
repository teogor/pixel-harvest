package dev.teogor.pixel.harvest.slash

import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.teogor.pixel.harvest.BotManager.kord
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.message.getBasePathForImages
import dev.teogor.pixel.harvest.svg.ProgressData
import dev.teogor.pixel.harvest.svg.ProgressListener
import dev.teogor.pixel.harvest.svg.SvgConverter
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

        // override val action: (ChatInputInteractionEvent) -> Unit
        //     get() = { event ->
        //
        //         event.reply("Converting to svg in progress...")
        //             .withEphemeral(true)
        //             .subscribe()
        //
        //     }
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

        // override val commandRequest: ApplicationCommandRequest
        //     get() = ApplicationCommandRequest.builder()
        //         .name(name)
        //         .description("Greets you")
        //         .addOption(
        //             ApplicationCommandOptionData.builder()
        //                 .name("name")
        //                 .description("Your name")
        //                 .type(ApplicationCommandOption.Type.STRING.value)
        //                 .required(true)
        //                 .build()
        //         ).build()
        //
        // override val action: (ChatInputInteractionEvent) -> Unit
        //     get() = { event ->
        //         val name = event.getStringParam("name")
        //
        //         event.reply("Greetings, $name!").withEphemeral(true).subscribe()
        //     }
    }

    object InfoSlashCommand : SlashCommand() {
        override val name: String = "info"

        override val description: String = "View information about your profile."

        //     override val commandRequest: ApplicationCommandRequest
        //         get() = ApplicationCommandRequest.builder()
        //             .name(name)
        //             .description("View information about your profile.")
        //             .build()
        //
        //     override val action: (ChatInputInteractionEvent) -> Unit
        //         get() = { event ->
        //             val basePath = "Downloads/${2
        //                 // client.getBasePathForImages(
        //                 //     event = event,
        //                 //     haveChannel = false
        //                 // )
        //             }".replace("\\", "/")
        //
        //             val author = event.interaction.user
        //             val username = author.username
        //
        //             val downloadCount = getTotalDownloadCountByDiscordUser(author.id.asLong())
        //
        //             val builder = EmbedCreateSpec.builder()
        //             builder.color(Color.BLACK)
        //             builder.title("Your Info - $username")
        //             builder.description(
        //                 """
        //                 **Lifetime Images Downloaded:** `$downloadCount`
        //
        //                 **Auto Download:**  `Active`
        //                 **Channel Subdirectory:**  `Disabled`
        //                 **Download Folder Root:**  `${basePath}`
        //                 """.trimIndent()
        //             )
        //             val spec = InteractionApplicationCommandCallbackSpec.builder()
        //                 .addEmbed(builder.build())
        //                 .ephemeral(true)
        //                 .build()
        //             event.reply(spec).block()
        //         }
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

fun Any?.asBooleanOrDefault(defaultValue: Boolean = false): Boolean {
    return try {
        this?.toString()?.toBoolean() ?: defaultValue
    } catch (e: IllegalArgumentException) {
        defaultValue
    }
}

fun Any?.asFloatOrDefault(defaultValue: Float = 0.0f): Float {
    return try {
        this?.toString()?.toFloat() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun Any?.asStringOrDefault(defaultValue: String = ""): String {
    return this?.toString() ?: defaultValue
}

fun Any?.asIntOrDefault(defaultValue: Int? = null): Int? {
    return try {
        this?.toString()?.toInt() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun Any?.asLongOrDefault(defaultValue: Long? = null): Long? {
    return try {
        this?.toString()?.toLong() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}



