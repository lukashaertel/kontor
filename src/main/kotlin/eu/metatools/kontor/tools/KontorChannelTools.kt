package eu.metatools.kontor.tools

import eu.metatools.kontor.To
import eu.metatools.kontor.ToAll
import eu.metatools.kontor.ToAllExcept
import eu.metatools.kontor.ToOnly
import io.netty.channel.Channel
import kotlinx.coroutines.experimental.channels.SendChannel

/**
 * Sends a [ToAll] envelope.
 */
suspend fun <T, A> SendChannel<To<T, A>>.sendAll(content: T) =
        send(ToAll(content))

/**
 * Sends a [ToOnly] envelope.
 */
suspend fun <T, A> SendChannel<To<T, A>>.sendOnly(content: T, to: A) =
        send(ToOnly(content, to))

/**
 * Sends a [ToAllExcept] envelope.
 */
suspend fun <T, A> SendChannel<To<T, A>>.sendAllExcept(content: T, except: A) =
        send(ToAllExcept(content, except))