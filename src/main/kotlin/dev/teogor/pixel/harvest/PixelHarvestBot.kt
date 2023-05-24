package dev.teogor.pixel.harvest

import dev.teogor.pixel.harvest.discord.PathUtils.getDownloadsFolderPath
import dev.teogor.pixel.harvest.message.MessageDiscordModule
import dev.teogor.pixel.harvest.slash.CommandDiscordModule
import dev.teogor.pixel.harvest.svg.SvgConverter
import discord4j.core.DiscordClient
import java.io.File

class PixelHarvestBot(token: String) {
    private val client: DiscordClient = DiscordClient.create(token)

    fun start() {
        val gateway = client.login().block() ?: return

        println("Logged In!")

        BotManager.client = client
        BotManager.gateway = gateway

        MessageDiscordModule().apply {
            bindGateway()
        }

        CommandDiscordModule().apply {
            bindGateway()
            setupTestCommands()
        }

        val rootPath = "${getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images".replace("\\", "/")
        val inputFolder = File(rootPath)
        val outputFolder = File("${rootPath}\\converter")

        SvgConverter.Builder(inputFolder, outputFolder)
            .withSvgGenerator(true)
            .withSvgRasterizer(true)
            .withBatchNumber(3)
            .build()

        keepBotAlive()
    }
}

private fun keepBotAlive() {
    while (true) {
        Thread.sleep(1000)
    }
}
