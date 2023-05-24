package dev.teogor.pixel.harvest.message

import dev.teogor.pixel.harvest.database.DatabaseManager.addUser
import dev.teogor.pixel.harvest.DiscordModule
import dev.teogor.pixel.harvest.discord.deleteMessageAfterDelay
import dev.teogor.pixel.harvest.models.Bot
import dev.teogor.pixel.harvest.models.Developer
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import java.time.Duration


class MessageDiscordModule(
    client: DiscordClient,
    gateway: GatewayDiscordClient,
) : DiscordModule(client, gateway) {

    override val events = listOf(
        MessageCreateEvent::class.java,
        ReactionAddEvent::class.java
    )

    override fun subscribeGateway(event: Event) {
        when (event) {
            is MessageCreateEvent -> onMessageReceived(event)
            is ReactionAddEvent -> deleteMessage(event)
        }
    }

    private fun onMessageReceived(event: MessageCreateEvent) {
        val message = event.message
        val authorId = message.author.orElse(null)?.id?.asLong() ?: return
        val author = message.author
        addUser(
            discordId = authorId,
            username = author.get().username,
        )
        if (Bot.MidJourneyBot.isBotIdMatch(authorId)) {
            ImageDownloader.downloadImages(
                client = client,
                event = event
            )
        } else if (Bot.NijiBot.isBotIdMatch(authorId)) {
            ImageDownloader.downloadImages(
                client = client,
                event = event
            )
        } else if (Developer.TeogorDeveloper.isDeveloperIdMatch(authorId)) {
            message.deleteMessageAfterDelay(Duration.ofSeconds(10))
        }
    }

    private fun deleteMessage(event: ReactionAddEvent) {
        val emoji = event.emoji
        if (emoji.asUnicodeEmoji().map { it.raw }.orElse("") == "❌") {
            // todo error stacktrace when added to Midjourney Bot
            event.message.block()?.delete()?.subscribe()
        }
    }
}
