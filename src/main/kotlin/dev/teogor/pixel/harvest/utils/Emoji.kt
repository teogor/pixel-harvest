package dev.teogor.pixel.harvest.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji

sealed class Emoji {
    abstract val id: Long
    abstract val name: String

    fun asReactionEmoji(): ReactionEmoji = ReactionEmoji.Custom(
        id = Snowflake(id),
        name = name,
        isAnimated = false,
    )

    object FileDownloadQueue : Emoji() {
        override val id: Long = 1110949956593930240
        override val name: String = "filedownloadqueue"
    }

    object FileDownloading : Emoji() {
        override val id: Long = 1110946830331695114
        override val name: String = "filedownloading"
    }

    object FileDownloaded : Emoji() {
        override val id: Long = 1110946827970285658
        override val name: String = "filedownloaded"
    }

    override fun equals(other: Any?): Boolean {
        if (other is ReactionEmoji.Custom) {
            return other.id.value.toLong() == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    companion object {

        fun fromReactionEmoji(emoji: ReactionEmoji.Custom): Emoji? {
            return when (emoji.id.value.toLong()) {
                FileDownloadQueue.id -> FileDownloadQueue
                FileDownloading.id -> FileDownloading
                FileDownloaded.id -> FileDownloaded
                else -> null
            }
        }

        fun isFileManager(emoji: ReactionEmoji.Custom): Boolean {
            return when (emoji.id.value.toLong()) {
                FileDownloadQueue.id -> true
                FileDownloading.id -> true
                FileDownloaded.id -> true
                else -> false
            }
        }

    }
}