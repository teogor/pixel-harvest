package dev.teogor.pixel.harvest

import dev.kord.core.Kord
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.teogor.pixel.harvest.message.MessageDiscordModule
import dev.teogor.pixel.harvest.slash.SlashDiscordModule
import kotlinx.coroutines.runBlocking

class PixelHarvestBot(private val token: String) {

    fun start() {
        runBlocking {
            BotManager.kord = Kord(token)
            val kord = BotManager.kord

            MessageDiscordModule().apply {
                bind()
            }

            SlashDiscordModule().apply {
                bind()
            }

            kord.login {
                @OptIn(PrivilegedIntent::class)
                intents += Intents.all
            }
        }
    }
}