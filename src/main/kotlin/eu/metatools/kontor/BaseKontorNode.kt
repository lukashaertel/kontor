package eu.metatools.kontor

import eu.metatools.kontor.serialization.InputBufferInput
import eu.metatools.kontor.serialization.OutputBufferOutput
import eu.metatools.kontor.serialization.serializerOf
import eu.metatools.kontor.server.Connected
import eu.metatools.kontor.server.Disconnected
import eu.metatools.kontor.server.Network
import eu.metatools.kontor.tools.awaitReady
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import rice.environment.Environment
import rice.p2p.commonapi.*
import rice.p2p.commonapi.Message
import rice.p2p.commonapi.Message.DEFAULT_PRIORITY
import rice.p2p.commonapi.rawserialization.MessageDeserializer
import rice.p2p.commonapi.rawserialization.OutputBuffer
import rice.p2p.commonapi.rawserialization.RawMessage
import rice.p2p.scribe.ScribeImpl
import rice.pastry.PastryNode
import rice.pastry.socket.SocketPastryNodeFactory
import rice.pastry.standard.RandomNodeIdFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.KSerializer

/**
 * Abstract base class of Pastry based Kontor nodes
 */
abstract class BaseKontorNode(
        val charset: Charset,
        val serializers: List<KSerializer<*>>,
        val environment: Environment) : KontorPastry<Any?, Any?> {
    companion object {
        val DEFAULT_ENVIRONMENT = Environment().apply {
            parameters.setString("nat_search_policy", "never")
        }
    }

    /**
     * Mutable node handle list for connection maintenance.
     */
    private val mutableNodeHandles = arrayListOf<NodeHandle>()

    /**
     * Associate by the serialized class, retain index for writing to streams.
     */
    private val indication = serializers.withIndex().associate { it.value.serializableClass to it }

    override val inbound = Channel<Any?>()

    override val outbound = actor<Any?>(CommonPool) {
        for (msg in channel)
            if (msg == null)
                endpoint?.route(null, NullMessage(), null)
            else {
                val (index, serializer) = indication.getValue(msg::class)
                for (nodeHandle in nodeHandles)
                    endpoint?.route(null, OutMessage(msg, (index + 1).toShort(), serializer), nodeHandle)
            }
    }

    /**
     * The channel to receive connection updates.
     */
    val network = Channel<Network<NodeHandle>>()

    /**
     * List of currently connected node handles.
     */
    val nodeHandles get() = mutableNodeHandles.toList()

    /**
     * Stores the pastry node running the messages.
     */
    private var node: PastryNode? = null

    /**
     * Stores the endpoint that sends messages.
     */
    private var endpoint: Endpoint? = null

    /**
     * The app wired into the local channels.
     */
    private val app = object : Application {
        override fun update(handle: NodeHandle, joined: Boolean) = runBlocking {
            if (joined) {
                mutableNodeHandles += handle
                network.send(Connected(nodeHandles, handle))
            } else {
                mutableNodeHandles -= handle
                network.send(Disconnected(nodeHandles, handle))
            }
        }

        override fun forward(message: RouteMessage) = true

        override fun deliver(id: Id, message: Message) {
            if (message is InMessage)
                runBlocking { inbound.send(message.item) }
            else
                throw IllegalArgumentException("Problems deserializing $message")
        }
    }

    /**
     * Outward null message.
     */
    private inner class NullMessage : RawMessage {
        override fun getPriority() = DEFAULT_PRIORITY

        override fun getType() = (-1).toShort()

        override fun serialize(buf: OutputBuffer) {}
    }


    /**
     * Outward message with item and, index of serializer and serializer.
     */
    private inner class OutMessage(val item: Any?, val index: Short, val serializer: KSerializer<*>) : RawMessage {
        override fun getPriority() = DEFAULT_PRIORITY

        override fun getType() = index

        override fun serialize(buf: OutputBuffer) {
            @Suppress("unchecked_cast")
            OutputBufferOutput(buf, charset).write(serializer as KSerializer<Any?>, item)
        }
    }

    /**
     * Inward message with item, priority and sender.
     */
    private inner class InMessage(val item: Any?, val messagePriority: Int) : Message {
        override fun getPriority() = messagePriority
    }

    /**
     * Deserializes a messages by looking up the type and using the respective serializer.
     */
    private val deserializer = MessageDeserializer { buf, type, priority, _ ->
        when (type.toInt()) {
        // Deserialize nulls as type `-1`
            -1 -> InMessage(null, priority)
        // Deserialize messages by serializer at position
            else -> {
                val serializer = serializers[type - 1]
                val item = InputBufferInput(buf, charset).read(serializer)
                InMessage(item, priority)
            }
        }
    }

    /**
     * Internal configuration method, used in subclasses to properly start and bootstrap the node.
     */
    protected fun start(boot: Boolean, bootAddress: String, bootPort: Int,
                        bindAddress: String, bindPort: Int, instance: String) = launch(CommonPool) {
        // Check if running
        if (node == null) {
            // Create factories
            val nodeIdFactory = RandomNodeIdFactory(environment)
            val nodeFactory = SocketPastryNodeFactory(nodeIdFactory, InetAddress.getByName(bindAddress), bindPort, environment)

            // Create node
            val node = nodeFactory.newNode()
            this@BaseKontorNode.node = node

            // Build endpoint on app
            val endpoint = node.buildEndpoint(app, instance)
            this@BaseKontorNode.endpoint = endpoint

            endpoint.deserializer = deserializer

            // Register endpoint
            endpoint.register()

            // Get list of bootstrap entries for boot flag
            val bootstrap = if (boot)
                emptyList()
            else
                listOf(InetSocketAddress(InetAddress.getByName(bootAddress), bootPort))

            // Boot to list
            node.boot(bootstrap)

            // Await ready
            awaitReady(node)
        }
    }


    override fun stop() = launch(CommonPool) {
        // Unregister existing nodes
        if (node != null || endpoint != null) {
            endpoint = null
            node = null
        }
    }
}