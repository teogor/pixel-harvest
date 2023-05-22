package dev.teogor.pixel.harvest.discord

import discord4j.core.`object`.entity.Message
import reactor.core.publisher.Mono
import java.time.Duration

fun Message.deleteMessageAfterDelay(delay: Duration) {
    Mono.delay(delay)
        .flatMap {
            delete()
        }
        .subscribe()
}