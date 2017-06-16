package eu.metatools.kontor

import io.netty.channel.ChannelFuture
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

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

    /**
     * Shuts down underlying workers, the respective class will not be usable after this.
     */
    fun shutdown(): Job = launch(Unconfined) {}
}

/**
 * Common interface for Kontor connectivity classes with network state management.
 */
interface KontorNetworked<out M> {
    /**
     * Channel of network change maintenance.
     */
    val management: ReceiveChannel<Network<M>>
}

/**
 * Common interface for JGroups bases Kontor connectivity classes.
 */
interface KontorJGroups<out In, in Out> : Kontor<In, Out> {
    /**
     * Joins a cluster.
     */
    fun start(clusterName: String): Job

    /**
     * Leaves a cluster.
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
}
