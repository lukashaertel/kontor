package eu.metatools.kontor.tools

import kotlinx.coroutines.experimental.channels.Channel

/**
 * Wrapper for calls to a paired data channel
 */
suspend fun <T, U> Channel<Pair<T, U>>.send(first: T, second: U) =
        send(first to second)