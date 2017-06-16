package eu.metatools.kontor

import io.netty.channel.ChannelFuture
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.Channel as DataChannel

/**
 * Common interface for Kontor connectivity classes.
 */
interface Kontor<out In, in Out> {
    /**
     * The inbound channel.
     */
    val inbound: ReceiveChannel<In>

    /**
     * The outbound channel.
     */
    val outbound: SendChannel<Out>
}

/**
 * Common interface for Pastry based Kontor connectivity classes.
 */
interface KontorPastry<out In, in Out> : Kontor<In, Out> {
    /**
     * Starts joining the ring and initializing the instance.
     */
    fun start(host:String, port: Int, instance: String): Job

    /**
     * Leaves the ring.
     */
    fun stop(): Job
}

/**
 * Common interface for Netty based Kontor connectivity classes.
 */
interface KontorNetty<out In, in Out> : Kontor<In, Out> {
    /**
     * Starts a connection or opens for connecting.
     */
    fun start(host: String, port: Int): ChannelFuture

    /**
     * Stops a connection or serving.
     */
    fun stop(): ChannelFuture

    /**
     * Shuts down underlying workers, the respective class will not be usable after this.
     */
    fun shutdown(): Job
}
