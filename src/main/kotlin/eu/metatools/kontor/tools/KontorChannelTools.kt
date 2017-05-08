package eu.metatools.kontor.tools

import eu.metatools.kontor.server.To
import eu.metatools.kontor.server.ToAll
import eu.metatools.kontor.server.ToAllExcept
import eu.metatools.kontor.server.ToOnly
import io.netty.channel.Channel
import kotlinx.coroutines.experimental.channels.SendChannel

/**
 * Sends a [ToAll] envelope.
 */
suspend fun <T> SendChannel<To<T>>.sendAll(content: T) =
        send(ToAll(content))

/**
 * Sends a [ToOnly] envelope.
 */
suspend fun <T> SendChannel<To<T>>.sendOnly(content: T, to: Channel) =
        send(ToOnly(content, to))

/**
 * Sends a [ToAllExcept] envelope.
 */
suspend fun <T> SendChannel<To<T>>.sendAllExcept(content: T, to: Channel) =
        send(ToAllExcept(content, to))