package eu.metatools.wepwawet2.components

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel

/**
 * Network interface for communication
 */
interface Net {
    /**
     * Local identity used for rejoins.
     */
    val identity: Any

    /**
     * Inbound data channel.
     */
    val inbound: ReceiveChannel<Any>

    /**
     * Outbound data channel.
     */
    val outbound: SendChannel<Any>

    /**
     * Returns a network safe random.
     */
    fun random(): Double

    /**
     * Returns a network safe random in the given range.
     */
    fun random(range: IntRange): Int

    /**
     * Returns a network safe random in the given long range.
     */
    fun random(range: LongRange): Long
}
