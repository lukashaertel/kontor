package eu.metatools.wepwawet2

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
     * The the root identity, this should not be returned by [getAndLeaseId] and should be equal for the entire group.
     */
    fun getRootId(): Id

    /**
     * Get and lease identity, it will become permanently unusable by other clients.
     */
    fun getAndLeaseId(): Id

    /**
     * Release identity, it can then be used by other clients.
     */
    fun releaseId(id: Id)

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
