package eu.metatools.kontor

import eu.metatools.kontor.serialization.KSerializationHandler
import eu.metatools.kontor.serialization.serializerOf
import eu.metatools.kontor.server.*
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
 * Provides network interaction as a server. [inbound] will receive all incoming messages, [outbound] will take all
 * outgoing messages. Network status may be queried by calling [channels] or by subscribing to [network].
 *
 * As server side sending requires designation, [inbound] and [outbound] are enveloped using [From] and [To]
 */
class KontorServer(
        val charset: Charset = Charsets.UTF_8,
        val serializers: List<KSerializer<*>>,
        val backlog: Int = 128) : Kontor<From, To> {
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
            inbound.send(From(msg, ctx.channel()))
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
    override val inbound = DataChannel<From>()

    /**
     * The outbound data broadcast channel.
     */
    override val outbound = actor<To>(CommonPool) {
        for (msg in channel)
            when (msg) {
                is ToAll -> for (c in channels) c.writeAndFlush(msg.content)
                is ToAllExcept -> for (c in channels) if (c != msg.except) c.writeAndFlush(msg.content)
                is ToOnly -> msg.to.writeAndFlush(msg.content)
            }

    }

    fun start(port: Int) = start("0.0.0.0", port)

    /**
     * Starts the server and returns the future notifying after the bind.
     */
    override fun start(host: String, port: Int): ChannelFuture {
        if (!groupsUsable)
            throw IllegalStateException("Workers are already shutdown")

        if (serverChannel != null)
            throw IllegalStateException("Server already started")

        return bootstrap.bind(host, port).apply {
            addListener {
                serverChannel = channel()
            }
        }
    }

    /**
     * Stops the server and returns the future notifying after the close.
     */
    override fun stop(): ChannelFuture {
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
    override fun shutdown() = launch(CommonPool) {
        // Close workers
        val w = workerGroup.shutdownGracefully()
        val b = bossGroup.shutdownGracefully()

        // Await their termination
        awaitAll(w, b)
    }
}
