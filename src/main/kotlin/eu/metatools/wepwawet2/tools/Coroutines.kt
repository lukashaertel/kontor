package eu.metatools.wepwawet2.tools

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.runBlocking

/**
 * Blocks if the channel's capacity is violated, otherwise returns immediately.
 */
fun <E> SendChannel<E>.offerSafe(element: E) {
    if (!offer(element))
        runBlocking { send(element) }
}