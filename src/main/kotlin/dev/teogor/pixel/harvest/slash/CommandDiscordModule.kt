package dev.teogor.pixel.harvest.slash

import dev.teogor.pixel.harvest.DiscordModule
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

class CommandDiscordModule : DiscordModule() {

    override val events = listOf(
        ChatInputInteractionEvent::class.java
    )

    override fun subscribeGateway(event: Event) {
        when (event) {
            is ChatInputInteractionEvent -> SlashCommand.executeCommand(event)
        }
    }

    fun setupTestCommands() {
        val guildId = 1064268807658557450L
        val applicationId = 1110114222442033172L

        val applicationService = gateway.restClient.applicationService
        SlashCommand.forEachCommand { command ->
            command.bindGuildApplicationCommand(
                applicationService = applicationService,
                guildId = guildId,
                applicationId = applicationId,
            )
        }
    }
}
