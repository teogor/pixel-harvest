package dev.teogor

import dev.teogor.pixel.harvest.PixelHarvestBot
import io.github.cdimascio.dotenv.dotenv

// todo discord bot info pages
//  https://discord.com/developers/applications/1110114222442033172/information
//  https://discord.com/oauth2/authorize?client_id=1110114222442033172&/scope=bot&permissions=8

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = dotenv()
        val botToken = dotenv["BOT_TOKEN"]
        if (botToken != null) {
            val botBeta = PixelHarvestBot(botToken)
            botBeta.start()
        } else {
            println("Please provide a token using the BOT_TOKEN environment variable.")
        }
    }
}
