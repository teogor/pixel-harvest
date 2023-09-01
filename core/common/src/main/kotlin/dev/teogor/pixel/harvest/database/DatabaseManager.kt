package dev.teogor.pixel.harvest.database

object DatabaseManager {
    private val databaseHandler: DatabaseHandler = DatabaseHandler("core/common/src/main/resources/pixel-harvest.db").apply {
        initializeDatabase()
    }

    fun addUser(discordId: Long, username: String) {
        databaseHandler.addUser(
            discordId = discordId,
            username = username
        )
    }

    fun addDownload(discordId: Long, url: String) {
        databaseHandler.addDownload(
            discordId = discordId,
            url = url
        )
    }

    fun getTotalDownloadCountByDiscordUser(discordId: Long): Long {
        return databaseHandler.getTotalDownloadCountByDiscordUser(
            discordId = discordId,
        )
    }
}