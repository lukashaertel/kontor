package eu.metatools.kontor.server

import io.netty.channel.Channel

/**
 * A `to` envelope, there are [ToAll] for broadcast, [ToAllExcept] for an excluding broadcast, and [ToOnly] for a
 * direct message.
 */
interface To<out T> {
    /**
     * The content of the envelope.
     */
    val content: T
}

/**
 * A broadcast envelope.
 */
data class ToAll<out T>(override val content: T) : To<T>

/**
 * A direct envelope.
 */
data class ToOnly<out T>(override val content: T, val to: Channel) : To<T>

/**
 * An excluding broadcast envelope.
 */
data class ToAllExcept<out T>(override val content: T, val except: Channel) : To<T>