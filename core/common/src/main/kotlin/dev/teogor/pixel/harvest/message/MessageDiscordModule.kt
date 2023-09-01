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
        var interactionAuthor: User
        var messageAuthor: User
        try {
            runBlocking {
                interactionAuthor = event.getUser()
                messageAuthor = event.getMessage().author!!
            }
        } catch (_: Throwable) {
            return
        }

        val emoji = event.emoji
        if (discriminateBots(messageAuthor, interactionAuthor, emoji)) {
            return
        }

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
                event.message.fetchMessageOrNull()?.let {
                    it.author?.let { author ->
                        val midjourneyBot = Bot.MidJourneyBot.isBotIdMatch(author.id.value.toLong())
                        val nijiBot = Bot.NijiBot.isBotIdMatch(author.id.value.toLong())
                        if (!midjourneyBot && !nijiBot) {
                            it.delete(reason = "marked with ❌")
                        }
                    }
                }
            }
        }
    }
}

fun discriminateBots(
    messageAuthor: User,
    interactionAuthor: User,
    emoji: ReactionEmoji
): Boolean {
    if (!interactionAuthor.isBot) {
        return false
    }
    if (Bot.MidJourneyBot.isBotIdMatch(interactionAuthor.id.value.toLong())) {
        return true
    } else if (Bot.NijiBot.isBotIdMatch(interactionAuthor.id.value.toLong())) {
        return true
    } else if(Bot.PixelHarvestBot.isBotIdMatch(interactionAuthor.id.value.toLong())) {
        if (emoji is ReactionEmoji.Custom) {
            if(Emoji.isFileManager(emoji)) {
                return true
            }
        }
    }
    return false
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