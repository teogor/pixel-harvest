package dev.teogor.pixel.harvest

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient

object BotManager {
    lateinit var client: DiscordClient
    lateinit var gateway: GatewayDiscordClient
}