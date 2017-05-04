package eu.metatools.kontor

import eu.metatools.kontor.serialization.KSerializationHandler
import eu.metatools.kontor.serialization.serializerOf
import eu.metatools.kontor.tools.*
import io.netty.channel.*
import io.netty.channel.ChannelOption.*
import kotlinx.coroutines.experimental.runBlocking
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.actor
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.KSerializer

import kotlinx.coroutines.experimental.channels.Channel as DataChannel

/**
 * Provides network interaction as a client. [inbound] will receive all incoming messages, [outbound] will take all
 * outgoing messages.
 */
class KontorClient(
        val charset: Charset = Charsets.UTF_8,
        val serializers: List<KSerializer<*>>) {
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
    private val workerGroup = NioEventLoopGroup()

    /**
     * True if worker is still usable.
     */
    private val groupUsable get() = !workerGroup.isShutdown &&
            !workerGroup.isShuttingDown &&
            !workerGroup.isTerminated


    /**
     * Main message transcoder.
     */
    private val transcoder = KSerializationHandler(charset, serializers)


    /**
     * The active client channel
     */
    private var clientChannel: Channel? = null

    /**
     * Inwards handler class.
     */
    private inner class InwardsHandler : ChannelInboundHandlerAdapter() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) = runBlocking {
            inbound.send(msg)
        }
    }

    /**
     * Main message handler.
     */
    private val handler = InwardsHandler()

    /**
     * Bootstrap setup for client.
     */
    private val bootstrap = bootstrap {
        group(workerGroup)
        channel(NioSocketChannel::class.java)
        option(SO_KEEPALIVE, true)
        handlerInit<SocketChannel> {
            pipeline += transcoder
            pipeline += handler
        }
    }

    /**
     * The inbound data channel.
     */
    val inbound = DataChannel<Any?>()

    /**
     * The outbound data broadcast channel.
     */
    val outbound = actor<Any?>(CommonPool) {
        for (msg in channel)
            clientChannel?.writeAndFlush(msg)
    }

    /**
     * Starts the client and returns the future notifying after the connect.
     */
    fun start(address: String, port: Int): ChannelFuture {
        if (!groupUsable)
            throw IllegalStateException("Workers are already shutdown")

        if (clientChannel != null)
            throw IllegalStateException("Client already connected")

        return bootstrap.connect(address, port).apply {
            addListener {
                clientChannel = channel()
            }
        }
    }

    /**
     * Suspends until the client is disconnected from a closed server.
     */
    fun disconnect(): ChannelFuture {
        if (!groupUsable)
            throw IllegalStateException("Workers are already shutdown")

        val currentChannel = clientChannel ?: throw IllegalStateException("Client is already stopped")

        return currentChannel.closeFuture()
    }

    /**
     * Stops the client and returns the future notifying after the close.
     */
    fun stop(): ChannelFuture {
        if (!groupUsable)
            throw IllegalStateException("Workers are already shutdown")

        val currentChannel = clientChannel ?: throw IllegalStateException("Client is already stopped")

        return currentChannel.close().apply {
            addListener {
                clientChannel = null
            }
        }
    }

    /**
     * Completely shuts down the workers.
     */
    fun shutdown() = workerGroup.shutdownGracefully()
}
