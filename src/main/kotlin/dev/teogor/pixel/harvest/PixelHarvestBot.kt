package dev.teogor.pixel.harvest

import dev.teogor.pixel.harvest.database.DatabaseHandler
import dev.teogor.pixel.harvest.discord.PathUtils.getBasePathForImages
import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.message.MessageDiscordModule
import dev.teogor.pixel.harvest.slash.CommandDiscordModule
import dev.teogor.pixel.harvest.svg.SvgConverter
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import java.io.File

object DatabaseManager {
    private val databaseHandler: DatabaseHandler = DatabaseHandler("src/main/resources/pixel-harvest.db").apply {
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

object BotManager {
    lateinit var client: DiscordClient
    lateinit var gateway: GatewayDiscordClient
}

class PixelHarvestBot(token: String) {
    private val client: DiscordClient = DiscordClient.create(token)

    fun start() {
        val gateway = client.login().block() ?: return

        println("Logged In!")

        BotManager.client = client
        BotManager.gateway = gateway

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

        val rootPath = "${getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images".replace("\\", "/")
        val inputFolder = File(rootPath)
        val outputFolder = File("${rootPath}\\converter")

        SvgConverter.Builder(inputFolder, outputFolder)
            .withSvgGenerator(true)
            .withSvgRasterizer(true)
            .withBatchNumber(1)
            .build()

        keepBotAlive()
    }
}

private fun keepBotAlive() {
    while (true) {
        Thread.sleep(1000)
    }
}
