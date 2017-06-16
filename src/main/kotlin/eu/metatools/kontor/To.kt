package eu.metatools.kontor

import io.netty.channel.Channel

/**
 * A `to` envelope, there are [ToAll] for broadcast, [ToAllExcept] for an excluding broadcast, and [ToOnly] for a
 * direct message.
 */
interface To<out T, out A> {
    /**
     * The content of the envelope.
     */
    val content: T
}

/**
 * A broadcast envelope.
 */
data class ToAll<out T, out A>(override val content: T) : To<T, A>

/**
 * A direct envelope.
 */
data class ToOnly<out T, out A>(override val content: T, val to: A) : To<T, A>

/**
 * An excluding broadcast envelope.
 */
data class ToAllExcept<out T, out A>(override val content: T, val except: A) : To<T, A>