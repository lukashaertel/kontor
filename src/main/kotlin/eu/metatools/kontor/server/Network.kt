package eu.metatools.kontor.server

import io.netty.channel.Channel

/**
 * A network update message.
 */
interface Network<out I> {
    /**
     * Lists the currently connected channels.
     */
    val channels: List<I>
}

/**
 * Network update message where a new connection was added.
 */
data class Connected<out I>(override val channels: List<I>, val channel: I) : Network<I>

/**
 * Network update message where a new connection was removed.
 */
data class Disconnected<out I>(override val channels: List<I>, val channel: I) : Network<I>