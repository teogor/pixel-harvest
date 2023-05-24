package dev.teogor.pixel.harvest.models

/**
 * Represents a bot entity.
 */
sealed class Bot {
    /**
     * The unique identifier of the bot.
     */
    abstract val id: Long

    /**
     * The name of the bot.
     */
    abstract val name: String

    /**
     * Represents the Midjourney bot.
     */
    object MidJourneyBot : Bot() {
        override val id: Long = 936929561302675456
        override val name: String = "Midjourney"
    }

    /**
     * Represents the Niji bot.
     */
    object NijiBot : Bot() {
        override val id: Long = 1022952195194359889
        override val name: String = "Niji"
    }

    companion object {
        private val botIds: List<Bot> = listOf(
            MidJourneyBot,
            NijiBot,
        )

        /**
         * Checks if a user ID corresponds to a known bot.
         *
         * @param userId The ID of the user to check.
         * @return A pair containing a Boolean indicating whether the user is a known bot, and the corresponding bot object if it is a known bot.
         *
         * Example usage:
         * ```
         * val userId = 936832561302675456
         * val (isBot, bot) = Bot.isKnownBot(userId)
         *
         * if (isBot) {
         *     println("The user is a known bot: ${bot?.name}")
         * } else {
         *     println("The user is not a known bot.")
         * }
         * ```
         */
        fun isKnownBot(userId: Long): Pair<Boolean, Bot?> {
            val bot = botIds.find { it.id == userId }
            return Pair(bot != null, bot)
        }

        /**
         * Retrieves the Bot object based on the provided bot ID.
         *
         * @param botId The ID of the bot.
         * @return The corresponding Bot object, or null if no bot with the ID is found.
         */
        fun getBotById(botId: Long): Bot? {
            return botIds.find { it.id == botId }
        }
    }

    /**
     * Checks if the bot's ID is the same as the provided user ID.
     *
     * @param authorId The ID of the user to compare with the bot's ID.
     * @return `true` if the user ID is the same as the bot's ID, `false` otherwise.
     */
    fun isBotIdMatch(authorId: Long): Boolean {
        return id == authorId
    }
}