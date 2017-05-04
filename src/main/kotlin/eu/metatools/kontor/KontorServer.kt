package eu.metatools.kontor

import eu.metatools.kontor.serialization.KSerializationHandler
import eu.metatools.kontor.serialization.serializerOf
import eu.metatools.kontor.tools.*
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelOption.SO_BACKLOG
import io.netty.channel.ChannelOption.SO_KEEPALIVE
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.KSerializer
import kotlinx.coroutines.experimental.channels.Channel as DataChannel

/**
 * Provides network interaction as a server. [inbound] will receive all incoming messages, [outbound],
 * [outboundDesignated] and [outboundInvDesignated] will take all outgoing messages. Network status may be queried by
 * calling [channels] or by subscribing to [network].
 */
class KontorServer(
        val charset: Charset = Charsets.UTF_8,
        val serializers: List<KSerializer<*>>,
        val backlog: Int = 128) {
    constructor(vararg serializers: KSerializer<*>)
            : this(serializers = listOf(*serializers))

    constructor(charset: Charset, vararg serializers: KSerializer<*>)
            : this(charset, listOf(*serializers))

    constructor(vararg kClasses: KClass<*>)
            : this(serializers = kClasses.map { serializerOf(it) })

    constructor(charset: Charset, vararg kClasses: KClass<*>)
            : this(charset, kClasses.map { serializerOf(it) })

    /**
     * Main group for server message handling.
     */
    private val bossGroup = NioEventLoopGroup()

    /**
     * Sub group for child channel handling.
     */
    private val workerGroup = NioEventLoopGroup()

    /**
     * True if workers are still usable.
     */
    private val groupsUsable get() = !workerGroup.isShutdown &&
            !bossGroup.isShutdown &&
            !workerGroup.isShuttingDown &&
            !bossGroup.isShuttingDown &&
            !workerGroup.isTerminated &&
            !bossGroup.isTerminated

    /**
     * Main message transcoder.
     */
    private val transcoder = KSerializationHandler(charset, serializers)

    /**
     * Mutable channel list for connection maintenance.
     */
    private val mutableChannels = arrayListOf<Channel>()


    /**
     * The active server channel
     */
    private var serverChannel: Channel? = null

    /**
     * Inwards handler, inner class to support sharable.
     */
    @Sharable
    private inner class InwardsHandler : ChannelInboundHandlerAdapter() {
        override fun channelActive(ctx: ChannelHandlerContext) = runBlocking {
            mutableChannels += ctx.channel()
            network.send(Connected(mutableChannels, ctx.channel()))
        }

        override fun channelInactive(ctx: ChannelHandlerContext) = runBlocking {
            mutableChannels -= ctx.channel()
            network.send(Disconnected(mutableChannels, ctx.channel()))
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) = runBlocking {
            inbound.send(ctx.channel(), msg)
        }
    }

    /**
     * Main message handler.
     */
    private val handler = InwardsHandler()

    /**
     * Bootstrap setup for server.
     */
    private val bootstrap = serverBootstrap {
        group(bossGroup, workerGroup)
        channel(NioServerSocketChannel::class.java)
        option(SO_BACKLOG, backlog)
        childOption(SO_KEEPALIVE, true)
        childHandlerInit<SocketChannel> {
            pipeline += transcoder
            pipeline += handler
        }
    }

    /**
     * List of currently connected client channels.
     */
    val channels get() = mutableChannels.toList()

    /**
     * The channel to receive connection updates.
     */
    val network = DataChannel<Network>()

    /**
     * The inbound data channel.
     */
    val inbound = DataChannel<Pair<Channel, Any?>>()

    /**
     * The outbound data broadcast channel.
     */
    val outbound = actor<Any?>(CommonPool) {
        for (msg in channel)
            for (c in channels)
                c.writeAndFlush(msg)
    }

    /**
     * The designated outbound data channel, messages will be sent to the channel in the pair only.
     */
    val outboundDesignated = actor<Pair<Channel, Any?>>(CommonPool) {
        for ((c, msg) in channel)
            c.writeAndFlush(msg)
    }

    /**
     * The invert desginated outbound data channel, messages will be sent to all channels except the one in the pair.
     */
    val outboundInvDesignated = actor<Pair<Channel, Any?>>(CommonPool) {
        for ((nc, msg) in channel)
            for (c in channels - nc)
                c.writeAndFlush(msg)
    }

    /**
     * Starts the server and returns the future notifying after the bind.
     */
    fun start(port: Int): ChannelFuture {
        if (!groupsUsable)
            throw IllegalStateException("Workers are already shutdown")

        if (serverChannel != null)
            throw IllegalStateException("Server already started")

        return bootstrap.bind(port).apply {
            addListener {
                serverChannel = channel()
            }
        }
    }

    /**
     * Stops the server and returns the future notifying after the close.
     */
    fun stop(): ChannelFuture {
        if (!groupsUsable)
            throw IllegalStateException("Workers are already shutdown")

        val currentChannel = serverChannel ?: throw IllegalStateException("Server is already stopped")

        return currentChannel.close().apply {
            addListener {
                serverChannel = null
            }
        }
    }

    /**
     * Completely shuts down the workers.
     */
    fun shutdown() = launch(CommonPool) {
        // Close workers
        val w = workerGroup.shutdownGracefully()
        val b = bossGroup.shutdownGracefully()

        // Await their termination
        awaitAll(w, b)
    }
}
