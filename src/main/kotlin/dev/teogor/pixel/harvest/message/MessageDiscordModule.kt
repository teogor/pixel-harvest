package dev.teogor.pixel.harvest.message

import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.teogor.pixel.harvest.DiscordModule
import kotlinx.coroutines.runBlocking
import dev.teogor.pixel.harvest.database.DatabaseManager.addUser
import dev.teogor.pixel.harvest.models.Bot
import dev.teogor.pixel.harvest.models.Developer
import kotlinx.coroutines.delay
import java.time.Duration

class MessageDiscordModule : DiscordModule() {
    override val events: List<Class<out Event>> = listOf(
        MessageCreateEvent::class.java,
        ReactionAddEvent::class.java
    )

    override fun subscribe(event: Event) {
        // Implement your event handling logic here
        when (event) {
            is MessageCreateEvent -> onMessageReceived(event)
            is ReactionAddEvent -> reactionHandler(event)
        }
    }

    private fun onMessageReceived(event: MessageCreateEvent) {
        val message = event.message
        val authorId = message.author?.id?.value?.toLong() ?: return
        val author = message.author!!
        addUser(
            discordId = authorId,
            username = author.username,
        )
        if (Bot.MidJourneyBot.isBotIdMatch(authorId)) {
            ImageDownloader.addToQueue(
                message = event.message
            )
        } else if (Bot.NijiBot.isBotIdMatch(authorId)) {
            ImageDownloader.addToQueue(
                message = event.message
            )
        } else if (Developer.TeogorDeveloper.isDeveloperIdMatch(authorId)) {
            message.deleteMessageAfterDelay(Duration.ofSeconds(10))
        }
    }

    private fun reactionHandler(event: ReactionAddEvent) {
        if (Bot.isKnownBot(event.userId.value.toLong()).first) {
            return
        }
        var user: User
        runBlocking {
            user = event.getUser()
        }
        if (user.isBot) {
            println(user)
        }

        val emoji = event.emoji
        if (emoji.name == "❌") {
            runBlocking {
                event.message.delete(reason = "marked with ❌")
            }
        } else if (emoji.name == "📁") {
            runBlocking {
                val message = event.message.fetchMessage()
                ImageDownloader.addToQueue(
                    message = message
                )
            }
        }
    }
}

fun Message.deleteMessageAfterDelay(delay: Duration) {
    runBlocking {
        delay(delay.toMillis())
        delete(reason = "autodelete for developer")
    }
}