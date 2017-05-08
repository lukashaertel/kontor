package eu.metatools.kontor.server

import io.netty.channel.Channel

/**
 * A `from` envelope for servers to disambiguate between the channel a message was received on.
 */
data class From<out T>(val content: T, val from: Channel)