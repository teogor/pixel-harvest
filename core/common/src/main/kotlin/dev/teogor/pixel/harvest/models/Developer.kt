package dev.teogor.pixel.harvest.models

/**
 * Represents a known developer entity.
 */
sealed class Developer {
    /**
     * The unique identifier of the developer.
     */
    abstract val id: Long

    /**
     * The name of the developer.
     */
    abstract val name: String

    /**
     * Represents the developer Teogor.
     */
    object TeogorDeveloper : Developer() {
        override val id: Long = 626721635860480021
        override val name: String = "Teogor"
    }

    companion object {
        private val developerIds: List<Developer> = listOf(
            TeogorDeveloper
        )

        /**
         * Checks if a user ID corresponds to a known developer.
         *
         * @param userId The ID of the user to check.
         * @return A pair containing a Boolean indicating whether the user is a known developer, and the corresponding developer object if it is a known developer.
         *
         * Example usage:
         * ```
         * val userId = 626721635860480021
         * val (isDeveloper, developer) = Developer.isKnownDeveloper(userId)
         *
         * if (isDeveloper) {
         *     println("The user is a known developer: ${developer?.name}")
         * } else {
         *     println("The user is not a known developer.")
         * }
         * ```
         */
        fun isKnownDeveloper(userId: Long): Pair<Boolean, Developer?> {
            val developer = developerIds.find { it.id == userId }
            return Pair(developer != null, developer)
        }

        /**
         * Retrieves the Developer object based on the provided developer ID.
         *
         * @param developerId The ID of the developer.
         * @return The corresponding Developer object, or null if no developer with the ID is found.
         */
        fun getDeveloperById(developerId: Long): Developer? {
            return developerIds.find { it.id == developerId }
        }
    }

    /**
     * Checks if the developer's ID is the same as the provided user ID.
     *
     * @param userId The ID of the user to compare with the developer's ID.
     * @return `true` if the user ID is the same as the developer's ID, `false` otherwise.
     */
    fun isDeveloperIdMatch(userId: Long): Boolean {
        return id == userId
    }
}