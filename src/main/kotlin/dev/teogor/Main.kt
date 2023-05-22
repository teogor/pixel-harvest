package dev.teogor

import dev.teogor.pixel.harvest.PixelHarvestBot
import io.github.cdimascio.dotenv.dotenv

// https://discord.com/oauth2/authorize?client_id=1110114222442033172&/scope=bot&permissions=8
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = dotenv()
        val botToken = dotenv["BOT_TOKEN"]
        if (botToken != null) {
            val bot = PixelHarvestBot(botToken)
            bot.start()
        } else {
            println("Please provide a token using the BOT_TOKEN environment variable.")
        }
    }
}
