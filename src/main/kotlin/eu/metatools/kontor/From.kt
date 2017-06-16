package eu.metatools.kontor

import io.netty.channel.Channel

/**
 * A `from` envelope for servers to disambiguate between the channel a message was received on.
 */
data class From<out T, out A>(val content: T, val from: A)