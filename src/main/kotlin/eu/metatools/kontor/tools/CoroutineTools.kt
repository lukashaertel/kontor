package eu.metatools.kontor.tools

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext


/**
 * Launches in the [context] a consumer feeding into [block].
 */
fun <T> ReceiveChannel<T>.launchConsumer(context: CoroutineContext, block: suspend (T) -> Unit) = launch(context) {
    consumeEach(block)
}

/**
 * [launchConsumer] with [CommonPool].
 */
fun <T> ReceiveChannel<T>.launchConsumer(block: suspend (T) -> Unit) = launchConsumer(CommonPool, block)