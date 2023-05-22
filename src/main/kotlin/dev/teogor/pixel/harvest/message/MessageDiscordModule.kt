package dev.teogor.pixel.harvest.message

import dev.teogor.pixel.harvest.DiscordModule
import dev.teogor.pixel.harvest.discord.deleteMessageAfterDelay
import dev.teogor.pixel.harvest.models.Bot
import dev.teogor.pixel.harvest.models.Developer
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.util.Color
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
        if (Bot.MidJourneyBot.isBotIdMatch(authorId)) {
            ImageDownloader.downloadImages(
                client = client,
                event = event
            )
        }
        if (Developer.TeogorDeveloper.isDeveloperIdMatch(authorId)) {
            message.deleteMessageAfterDelay(Duration.ofSeconds(10))
            sendEmbeddedMessage(event)
        }

    }

    private fun sendEmbeddedMessage(event: MessageCreateEvent) {
        val channel = event.message.channel.block() ?: return
        val author = event.message.author.get()
        val username = author.username
        val discriminator = author.discriminator
        val usernameDiscord = "$username#$discriminator"
        val builder = EmbedCreateSpec.builder()
        builder.color(Color.BLACK)
        builder.title("Your Info - $usernameDiscord")
        builder.description(
            """
            **Files Downloaded:** 32742
            **Images Downloaded:** 23948
        
            **Auto Download:**  `Active`
            **Download Folder Root:**  `Downloads`
            **Channel Subdirectory:**  `Disabled`
            """.trimIndent()
        )

        channel.createMessage(builder.build())?.block()
    }

    private fun deleteMessage(event: ReactionAddEvent) {
        val emoji = event.emoji
        if (emoji.asUnicodeEmoji().map { it.raw }.orElse("") == "❌") {
            // todo error stacktrace when added to Midjourney Bot
            event.message.block()?.delete()?.subscribe()
        }
    }
}
