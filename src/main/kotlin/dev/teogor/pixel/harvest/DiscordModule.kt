package dev.teogor.pixel.harvest

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ApplicationCommandCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on

abstract class DiscordModule {
    abstract val events: List<Class<out Event>>

    val kord: Kord = BotManager.kord

    private inline fun <reified T : Event> subscribeEvent(noinline handler: suspend T.() -> Unit) {
        kord.on<T> {
            handler()
        }
    }

    fun bind() {
        events.forEach { eventClass ->
            when (eventClass) {
                MessageCreateEvent::class.java -> {
                    subscribeEvent<MessageCreateEvent> {
                        subscribe(this)
                    }
                }

                ReadyEvent::class.java -> {
                    subscribeEvent<ReadyEvent> {
                        subscribe(this)
                    }
                }

                ReactionAddEvent::class.java -> {
                    subscribeEvent<ReactionAddEvent> {
                        subscribe(this)
                    }
                }

                ApplicationCommandCreateEvent::class.java -> {
                    subscribeEvent<ApplicationCommandCreateEvent> {
                        subscribe(this)
                    }
                }

                GuildChatInputCommandInteractionCreateEvent::class.java -> {
                    subscribeEvent<GuildChatInputCommandInteractionCreateEvent> {
                        subscribe(this)
                    }
                }
                // Add more event types and their corresponding subscribeEvent calls as needed
                else -> error("Unsupported event type: ${eventClass.simpleName}")
            }
        }
    }

    open fun subscribe(event: Event) {
        // Implement custom logic for handling each event type
    }
}