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
suspend fun SendChannel<To>.sendAll(content: Any?) =
        send(ToAll(content))

/**
 * Sends a [ToOnly] envelope.
 */
suspend fun SendChannel<To>.sendOnly(content: Any?, to: Channel) =
        send(ToOnly(content, to))

/**
 * Sends a [ToAllExcept] envelope.
 */
suspend fun SendChannel<To>.sendAllExcept(content: Any?, to: Channel) =
        send(ToAllExcept(content, to))