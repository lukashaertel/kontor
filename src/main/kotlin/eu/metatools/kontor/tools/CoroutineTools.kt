package eu.metatools.kontor.tools

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext


/**
 * Launches in the [context] a consumer feeding into [block].
 */
fun <T> ReceiveChannel<T>.launchConsumer(context: CoroutineContext, block: suspend (T) -> Unit) =
        launch(context) {
            consumeEach(block)
        }

/**
 * [launchConsumer] with [CommonPool].
 */
fun <T> ReceiveChannel<T>.launchConsumer(block: suspend (T) -> Unit) =
        launchConsumer(CommonPool, block)

/**
 * A receive channel that has a job running along with it.
 * @param E The type of the channel
 * @param job The job that is running along with the channel
 * @param receiveChannel The original receive channel
 */
class JobBoundReceiveChannel<out E>(
        val job: Job,
        val receiveChannel: ReceiveChannel<E>) : ReceiveChannel<E> by receiveChannel, Job by job

/**
 * Consumes from a channel of [T] all elements of type [U], returns a second channel where only non-[U] elements are
 * left over. Consumption is done in a [Job].
 * @param T The type of the input elements
 * @param U The type of elements to consume in the block
 * @receiver The channel to consume from
 * @param context The context to run the consumer job in
 * @param block The block to run for the elements of type [U]
 * @return Returns a [Job] paired with a [ReceiveChannel] providing all remaining elements.
 */
inline fun <T, reified U : T> ReceiveChannel<T>.choose(
        context: CoroutineContext, noinline block: suspend (U) -> Unit) =
        Channel<T>().let { otherwise ->
            JobBoundReceiveChannel(launchConsumer(context) {
                if (it is U)
                    block(it)
                else
                    otherwise.send(it)
            }, otherwise)
        }

/**
 * Consumes from a channel of [T] all elements of type [U], returns a second channel where only non-[U] elements are
 * left over. Consumption is done in a [Job].
 * @param T The type of the input elements
 * @param U The type of elements to consume in the block
 * @receiver The channel to consume from
 * @param block The block to run for the elements of type [U]
 * @return Returns a [Job] paired with a [ReceiveChannel] providing all remaining elements.
 */
inline infix fun <T, reified U : T> ReceiveChannel<T>.choose(noinline block: suspend (U) -> Unit) =
        choose(CommonPool, block)

/**
 * Consumes all elements from a channel of [T], only for those that are of type [U], executes the block. If non-[U]
 * elements are of interest, [choose] should be used.
 */
inline fun <T, reified U : T> ReceiveChannel<T>.pick(
        context: CoroutineContext, noinline block: suspend (U) -> Unit) =
        launchConsumer {
            if (it is U)
                block(it)
        }

/**
 * Consumes all elements from a channel of [T], only for those that are of type [U], executes the block.
 */
inline infix fun <T, reified U : T> ReceiveChannel<T>.pick(noinline block: suspend (U) -> Unit) =
        pick(CommonPool, block)

/**
 * From a channel reads all items discarding them.
 */
fun ReceiveChannel<*>.discardRemaining(context: CoroutineContext) =
        launchConsumer(context) { }

/**
 * From a channel reads all items discarding them.
 */
fun ReceiveChannel<*>.discardRemaining() =
        launchConsumer { }