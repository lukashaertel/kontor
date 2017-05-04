package eu.metatools.kontor.server

import io.netty.channel.Channel

/**
 * A `to` envelope, there are [ToAll] for broadcast, [ToAllExcept] for an excluding broadcast, and [ToOnly] for a
 * direct message.
 */
interface To {
    /**
     * The content of the envelope.
     */
    val content: Any?
}

/**
 * A broadcast envelope.
 */
data class ToAll(override val content: Any?) : To

/**
 * A direct envelope.
 */
data class ToOnly(override val content: Any?, val to: Channel) : To

/**
 * An excluding broadcast envelope.
 */
data class ToAllExcept(override val content: Any?, val except: Channel) : To