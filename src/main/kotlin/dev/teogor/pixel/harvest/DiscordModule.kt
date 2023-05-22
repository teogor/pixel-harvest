package dev.teogor.pixel.harvest

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import kotlinx.coroutines.runBlocking

abstract class DiscordModule(
    val client: DiscordClient,
    val gateway: GatewayDiscordClient,
) {
    abstract val events: List<Class<out Event>>

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