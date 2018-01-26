package eu.metatools.kontor

import eu.metatools.kontor.serialization.KSerializationHandler
import eu.metatools.kontor.tools.*
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
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
import kotlinx.serialization.KSerializer
import kotlinx.coroutines.experimental.channels.Channel as DataChannel

/**
 * Provides network interaction as a server. [inbound] will receive all incoming messages, [outbound] will take all
 * outgoing messages. Network status may be queried by calling [channels] or by subscribing to [management].
 *
 * As server side sending requires designation, [inbound] and [outbound] are enveloped using [From] and [To]
 */
class KontorServer(
        val charset: Charset = Charsets.UTF_8,
        val classes: List<KClass<*>>,
        val backlog: Int = 128) : KontorNetty<From<Any, Channel>, To<Any, Channel>>, KontorNetworked<Channel> {

    constructor(vararg classes: KClass<*>) : this(
            classes = listOf(*classes))

    constructor(charset: Charset, vararg classes: KClass<*>) : this(
            charset = charset,
            classes = listOf(*classes))

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
    private val groupsUsable
        get() = !workerGroup.isShutdown &&
                !bossGroup.isShutdown &&
                !workerGroup.isShuttingDown &&
                !bossGroup.isShuttingDown &&
                !workerGroup.isTerminated &&
                !bossGroup.isTerminated

    /**
     * Main message transcoder.
     */
    private val transcoder = KSerializationHandler(charset, classes)

    /**
     * Mutable channel list for connection maintenance.
     */
    private var mutableChannels = listOf<Channel>()


    /**
     * The active server channel
     */
    private var serverChannel: Channel? = null

    /**
     * Inwards handler, inner class to support sharable.
     */
    @Sharable
    private inner class InwardsHandler : ChannelInboundHandlerAdapter() {
        override fun channelActive(ctx: ChannelHandlerContext) {
            // Mutate channel state
            mutableChannels += ctx.channel()

            if (!management.isClosedForSend) {
                // Bind for job
                val channels = channels
                val channel = ctx.channel()

                // Launch send job
                launch(CommonPool) {
                    management.send(Connected(channels, channel))
                }
            }
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            // Mutate channel state
            mutableChannels -= ctx.channel()

            if (!management.isClosedForSend) {
                // Bind for job
                val channels = channels
                val channel = ctx.channel()

                // Launch send job
                launch(CommonPool) {
                    management.send(Disconnected(channels, channel))
                }
            }
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
    override val management = DataChannel<Network<Channel>>()

    /**
     * The inbound data channel.
     */
    override val inbound = DataChannel<From<Any, Channel>>()

    /**
     * The outbound data broadcast channel.
     */
    override val outbound = actor<To<Any, Channel>>(CommonPool) {
        for (msg in channel)
            when (msg) {
                is ToAll<Any, Channel> -> for (c in channels) c.writeAndFlush(msg.content)
                is ToAllExcept<Any, Channel> -> for (c in channels) if (c != msg.except) c.writeAndFlush(msg.content)
                is ToOnly<Any, Channel> -> msg.to.writeAndFlush(msg.content)
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
