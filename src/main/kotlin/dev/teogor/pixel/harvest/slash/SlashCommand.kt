package dev.teogor.pixel.harvest.slash

import dev.teogor.pixel.harvest.BotManager
import dev.teogor.pixel.harvest.BotManager.client
import dev.teogor.pixel.harvest.DatabaseManager.getTotalDownloadCountByDiscordUser
import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.discord.PathUtils.getBasePathForImages
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.service.ApplicationService
import discord4j.rest.util.Color

sealed class SlashCommand {
    abstract val name: String
    abstract val commandRequest: ApplicationCommandRequest
    abstract val action: (ChatInputInteractionEvent) -> Unit

    fun bindGuildApplicationCommand(
        applicationService: ApplicationService,
        guildId: Long,
        applicationId: Long,
    ) {
        applicationService.createGuildApplicationCommand(
            applicationId,
            guildId,
            commandRequest
        ).subscribe()
    }

    fun execute(event: ChatInputInteractionEvent) {
        action(event)
    }

    fun ChatInputInteractionEvent.getStringParam(name: String): String {
        return getOption(name)
            .flatMap { obj -> obj.value }
            .map { obj -> obj.asString() }
            .get()
    }

    object GreetSlashCommand : SlashCommand() {
        override val name: String = "greet"

        override val commandRequest: ApplicationCommandRequest
            get() = ApplicationCommandRequest.builder()
                .name(name)
                .description("Greets you")
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Your name")
                        .type(ApplicationCommandOption.Type.STRING.value)
                        .required(true)
                        .build()
                ).build()

        override val action: (ChatInputInteractionEvent) -> Unit
            get() = { event ->
                val name = event.getStringParam("name")

                event.reply("Greetings, $name!").withEphemeral(true).subscribe()
            }
    }

    object SettingsSlashCommand : SlashCommand() {
        override val name: String = "settings"

        override val commandRequest: ApplicationCommandRequest
            get() = ApplicationCommandRequest.builder()
                .name(name)
                .description("View and adjust your personal settings.")
                .build()

        override val action: (ChatInputInteractionEvent) -> Unit
            get() = { event ->
                event.reply("Adjust your settings here.").withEphemeral(true).subscribe()
            }
    }

    object InfoSlashCommand : SlashCommand() {
        override val name: String = "info"

        override val commandRequest: ApplicationCommandRequest
            get() = ApplicationCommandRequest.builder()
                .name(name)
                .description("View information about your profile.")
                .build()

        override val action: (ChatInputInteractionEvent) -> Unit
            get() = { event ->
                val basePath = "Downloads/${
                    client.getBasePathForImages(
                        event = event,
                        haveChannel = false
                    )
                }".replace("\\", "/")

                val author = event.interaction.user
                val username = author.username

                val downloadCount = getTotalDownloadCountByDiscordUser(author.id.asLong())

                val builder = EmbedCreateSpec.builder()
                builder.color(Color.BLACK)
                builder.title("Your Info - $username")
                builder.description(
                    """
                    **Lifetime Images Downloaded:** `$downloadCount`
            
                    **Auto Download:**  `Active`
                    **Channel Subdirectory:**  `Disabled`
                    **Download Folder Root:**  `${basePath}`
                    """.trimIndent()
                )
                val spec = InteractionApplicationCommandCallbackSpec.builder()
                    .addEmbed(builder.build())
                    .ephemeral(true)
                    .build()
                event.reply(spec).block()
            }
    }

    companion object {
        private val commands: List<SlashCommand> = listOf(
            GreetSlashCommand,
            SettingsSlashCommand,
            InfoSlashCommand,
        )

        fun forEachCommand(action: (SlashCommand) -> Unit) {
            commands.forEach {
                action(it)
            }
        }

        fun executeCommand(event: ChatInputInteractionEvent) {
            commands.filter {
                it.name == event.commandName
            }.forEach {
                it.execute(event)
            }
        }
    }
}