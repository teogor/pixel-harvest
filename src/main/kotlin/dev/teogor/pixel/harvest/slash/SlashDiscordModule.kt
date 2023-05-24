package dev.teogor.pixel.harvest.slash

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.teogor.pixel.harvest.DiscordModule
import kotlinx.coroutines.runBlocking

val TEST_SERVER_ID: Snowflake = Snowflake(1064268807658557450L)
val TEST_APPLICATION_ID: Snowflake = Snowflake(1110114222442033172L)

class SlashDiscordModule : DiscordModule() {
    override val events: List<Class<out Event>> = listOf(
        GuildChatInputCommandInteractionCreateEvent::class.java,
        ReadyEvent::class.java,
    )

    override fun subscribe(event: Event) {
        when (event) {
            is ReadyEvent -> registerSlashCommands(event)
            is GuildChatInputCommandInteractionCreateEvent -> applicationCommandEvent(event)
        }
    }

    private fun applicationCommandEvent(event: GuildChatInputCommandInteractionCreateEvent) {
        SlashCommand.executeCommand(event)
    }

    private fun registerSlashCommands(event: ReadyEvent) {
        event.apply {
            this.guilds.map { guild ->
                SlashCommand.forEachCommand { command ->
                    runBlocking {
                        guild.kord.createGuildChatInputCommand(
                            guildId = guild.id,
                            name = command.name,
                            description = command.description,
                        ) {
                            command.withOptions(this)
                        }
                    }
                }
            }
        }
    }
}
