package dev.teogor.pixel.harvest.message

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.teogor.pixel.harvest.DiscordModule
import dev.teogor.pixel.harvest.database.DatabaseManager.addUser
import dev.teogor.pixel.harvest.models.Bot
import dev.teogor.pixel.harvest.models.Developer
import dev.teogor.pixel.harvest.utils.Emoji
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

    object ImagineChannel {

        val id = Snowflake(1111055839302787133)

    }

    private fun onMessageReceived(event: MessageCreateEvent) {
        val message = event.message
        println(message)
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
            // message.deleteMessageAfterDelay(Duration.ofSeconds(10))
        }
    }

    private fun reactionHandler(event: ReactionAddEvent) {
        if (Bot.isKnownBot(event.toSafeId()).first) {
            return
        }
        var user: User
        runBlocking {
            user = event.getUser()
        }
        if (user.isBot) {
            println(user)
        }
        println(user)

        val emoji = event.emoji

        if (emoji is ReactionEmoji.Custom) {
            val relevantEmoji = Emoji.fromReactionEmoji(emoji)
            if (relevantEmoji is Emoji.FileDownloadQueue) {
                runBlocking {
                    ImageDownloader.addToQueue(
                        message = event.message.fetchMessage()
                    )
                }
            }
        } else if (emoji.name == "❌") {
            runBlocking {
                event.message.delete(reason = "marked with ❌")
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

fun <T : Event> T.toSafeId(): Long {
    if (this is ReactionAddEvent) {
        this.userId.value.toLong()
    }
    return 0
}