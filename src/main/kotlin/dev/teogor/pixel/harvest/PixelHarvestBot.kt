package dev.teogor.pixel.harvest

import dev.teogor.pixel.harvest.database.DatabaseHandler
import dev.teogor.pixel.harvest.message.MessageDiscordModule
import dev.teogor.pixel.harvest.slash.CommandDiscordModule
import discord4j.core.DiscordClient

class PixelHarvestBot(token: String) {
    private val client: DiscordClient = DiscordClient.create(token)

    fun start() {
        val gateway = client.login().block() ?: return

        println("Logged In!")

        val databaseHandler = DatabaseHandler("src/main/resources/pixel-harvest.db")
        databaseHandler.initializeDatabase()

        MessageDiscordModule(
            client = client,
            gateway = gateway,
        ).apply {
            bindGateway()
        }

        CommandDiscordModule(
            client = client,
            gateway = gateway,
        ).apply {
            bindGateway()
            setupTestCommands()
        }

        keepBotAlive()
    }
}

private fun keepBotAlive() {
    while (true) {
        Thread.sleep(1000)
    }
}
