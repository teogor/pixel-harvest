package dev.teogor.pixel.harvest.discord

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient

/**
 * Extension function to get the name of a channel by its ID.
 *
 * @param channelId The ID of the channel.
 * @return The name of the channel, or null if the channel is not found or the name cannot be retrieved.
 */
fun DiscordClient.getChannelNameById(channelId: Snowflake): String {
    return getChannelById(channelId).data.cache().block()?.name()?.get() ?: "Unknown Channel"
}