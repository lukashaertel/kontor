package eu.metatools.kontor.tools

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.util.concurrent.EventExecutorGroup
import io.netty.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Configures a client using a bootstrap configuration block.
 */
inline fun bootstrap(block: Bootstrap.() -> Unit): Bootstrap =
        Bootstrap().apply(block).validate()

/**
 * Configures a server using a server bootstrap configuration block.
 */
inline fun serverBootstrap(block: ServerBootstrap.() -> Unit): ServerBootstrap =
        ServerBootstrap().apply(block).validate()

/**
 * Initializes a child handler with a new channel initializer, overriding [ChannelInitializer.initChannel].
 */
inline fun <C : Channel>
        ServerBootstrap.childHandlerInit(crossinline init: C.() -> Unit): ServerBootstrap =
        childHandler(object : ChannelInitializer<C>() {
            override fun initChannel(ch: C) {
                init(ch)
            }
        })

/**
 * Initializes a handler with a new channel initializer, overriding [ChannelInitializer.initChannel].
 */
inline fun <C : Channel>
        ServerBootstrap.handlerInit(crossinline init: C.() -> Unit): ServerBootstrap =
        handler(object : ChannelInitializer<C>() {
            override fun initChannel(ch: C) {
                init(ch)
            }
        })

/**
 * Initializes a handler with a new channel initializer, overriding [ChannelInitializer.initChannel].
 */
inline fun <C : Channel>
        Bootstrap.handlerInit(crossinline init: C.() -> Unit): Bootstrap =
        handler(object : ChannelInitializer<C>() {
            override fun initChannel(ch: C) {
                init(ch)
            }
        })

/**
 * Suspends until the given Netty future is done and notifies it's listeners.
 */
suspend fun <V> await(future: Future<V>) = suspendCoroutine<V> { c ->
    future.addListener {
        if (future.isSuccess)
            c.resume(future.get())
        else
            c.resumeWithException(future.cause())
    }
}

/**
 * Suspends until all the given Netty futures are done and notified their listeners.
 */
suspend inline fun <reified V> awaitAll(futures: List<Future<V>>) = suspendCoroutine<List<V>> { c ->
    if (futures.isEmpty()) {
        // Immediately resume if no future is given
        c.resume(emptyList())
    } else {
        // Make result buffer and result counter
        val r = arrayOfNulls<V>(futures.size)
        val x = AtomicInteger(0)

        // Add listeners to all futures
        for ((i, future) in futures.withIndex())
            future.addListener {
                if (future.isSuccess) {
                    // If future is success, put result value
                    r[i] = future.get()

                    // Check if all results are present
                    if (x.incrementAndGet() == r.size)
                        c.resume(r.asList().map { it!! })
                } else {
                    // If future is fail, make resume impossible and fail with exception
                    x.set(r.size + 1)
                    c.resumeWithException(future.cause())
                }
            }
    }
}

/**
 * Suspends until all the given Netty futures are done and notified their listeners.
 */
suspend inline fun <reified V> awaitAll(vararg futures: Future<V>) = awaitAll(listOf(*futures))


/**
 * Suspends until all the given Netty futures are done and notified their listeners.
 */
@kotlin.jvm.JvmName("awaitAllUnit")
suspend fun awaitAll(futures: List<Future<*>>) = suspendCoroutine<Unit> { c ->
    if (futures.isEmpty()) {
        // Immediately resume if no future is given
        c.resume(Unit)
    } else {
        // Make result counter
        val x = AtomicInteger(0)

        // Add listeners to all futures
        for (future in futures)
            future.addListener {
                if (future.isSuccess) {
                    // Check if all results are present
                    if (x.incrementAndGet() == futures.size)
                        c.resume(Unit)
                } else {
                    // If future is fail, make resume impossible and fail with exception
                    x.set(futures.size + 1)
                    c.resumeWithException(future.cause())
                }
            }
    }
}

/**
 * Suspends until all the given Netty futures are done and notified their listeners.
 */
@kotlin.jvm.JvmName("awaitAllUnit")
suspend fun awaitAll(vararg futures: Future<*>) = awaitAll(listOf(*futures))


/**
 * Suspends until the given Netty future is done and notifies it's listeners, uses the resulting channel.
 */
suspend fun await(channelFuture: ChannelFuture) = suspendCoroutine<Channel> { c ->
    channelFuture.addListener {
        if (channelFuture.isSuccess)
            c.resume(channelFuture.channel())
        else
            c.resumeWithException(channelFuture.cause())
    }
}

/**
 * Suspends until all the given Netty futures are done and notified their listeners, uses the resulting channel.
 */
@kotlin.jvm.JvmName("awaitAllChannels")
suspend fun awaitAll(futures: List<ChannelFuture>) = suspendCoroutine<List<Channel>> { c ->
    // See code of await all for regular futures
    if (futures.isEmpty()) {
        c.resume(emptyList())
    } else {
        val r = arrayOfNulls<Channel>(futures.size)
        val x = AtomicInteger(0)

        for ((i, future) in futures.withIndex())
            future.addListener {
                if (future.isSuccess) {
                    r[i] = future.channel()
                    if (x.incrementAndGet() == r.size)
                        c.resume(r.asList().map { it!! })
                } else {
                    x.set(r.size + 1)
                    c.resumeWithException(future.cause())
                }
            }
    }
}

/**
 * Suspends until all the given Netty futures are done and notified their listeners, uses the resulting channel.
 */
@kotlin.jvm.JvmName("awaitAllChannels")
suspend fun awaitAll(vararg futures: ChannelFuture) = awaitAll(listOf(*futures))

/**
 * Wraps the function call returning the channels pipeline.
 */
val Channel.pipeline: ChannelPipeline get() = pipeline()

/**
 * Adds the handler to the pipeline.
 */
operator fun ChannelPipeline.plusAssign(channelHandler: ChannelHandler) {
    addLast(channelHandler)
}

/**
 * Adds the handlers to the pipeline.
 */
operator fun ChannelPipeline.plusAssign(channelHandler: List<ChannelHandler>) {
    addLast(*channelHandler.toTypedArray())
}

/**
 * Adds the named handler to the pipeline.
 */
operator fun ChannelPipeline.plusAssign(namedChannelHandler: Pair<String, ChannelHandler>) {
    addLast(namedChannelHandler.first, namedChannelHandler.second)
}

/**
 * Composes the channel pipeline with an executor group for parameter binding.
 */
data class PipelineWithGroup(
        val channelPipeline: ChannelPipeline,
        val eventExecutorGroup: EventExecutorGroup) {

    /**
     * Adds the handlers to the pipeline with the currently bound event executor group.
     */
    fun add(channelHandler: ChannelHandler) {
        channelPipeline.addLast(eventExecutorGroup, channelHandler)
    }

    /**
     * Adds the handlers to the pipeline with the currently bound event executor group.
     */
    fun add(channelHandler: List<ChannelHandler>) {
        channelPipeline.addLast(eventExecutorGroup, *channelHandler.toTypedArray())
    }

    /**
     * Adds the named handler to the pipeline
     */
    fun add(namedChannelHandler: Pair<String, ChannelHandler>) {
        channelPipeline.addLast(eventExecutorGroup, namedChannelHandler.first, namedChannelHandler.second)
    }

}

/**
 * Binds the event executor group in calls to channel pipeline adds.
 */
inline fun ChannelPipeline.with(group: EventExecutorGroup, configure: PipelineWithGroup.() -> Unit) {
    PipelineWithGroup(this, group).apply(configure)
}
