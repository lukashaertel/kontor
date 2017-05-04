package eu.metatools.kontor.server

import io.netty.channel.Channel

/**
 * A network update message.
 */
interface Network {
    /**
     * Lists the currently connected channels.
     */
    val channels: List<Channel>
}

/**
 * Network update message where a new connection was added.
 */
data class Connected(override val channels: List<Channel>, val channel: Channel) : Network

/**
 * Network update message where a new connection was removed.
 */
data class Disconnected(override val channels: List<Channel>, val channel: Channel) : Network