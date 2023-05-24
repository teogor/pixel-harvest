package dev.teogor.pixel.harvest

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import kotlinx.coroutines.runBlocking

abstract class DiscordModule {
    abstract val events: List<Class<out Event>>

    val client: DiscordClient = BotManager.client
    val gateway: GatewayDiscordClient = BotManager.gateway

    fun bindGateway() {
        events.forEach {
            gateway.eventDispatcher.on(it)
                .subscribe {
                    runBlocking {
                        subscribeGateway(it)
                    }
                }
        }
    }

    open fun subscribeGateway(event: Event) {

    }
}