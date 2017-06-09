package eu.metatools.wepwawet

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

interface Ctrl

interface Lobby {
    /**
     * Gets all currently assigned values in the lobby
     */
    val current: Map<String, Any?>

    /**
     * Sends a key-value assignment
     */
    fun send(key: String, value: Any?)

    /**
     * Accept current game status.
     */
    fun accept()

    /**
     * Decline current game status.
     */
    fun decline()

    /**
     *
     */
    fun connect(send: SendChannel<Ctrl>, receive: ReceiveChannel<Ctrl>)

    /**
     * Call to disconnect from a server.
     */
    fun disconnect()

    /**
     * True if lobby should continue running.
     */
    val isInLobby: Boolean

    /**
     * Updates the lobby.
     */
    fun update()
}

/**
 * The gam controller.
 */
interface Game {
    /**
     * Initializes the game with a game entity initialized on the agreed lobby value.
     */
    fun <T : Entity> start(game: (Node, Map<String, Any?>) -> T): T

    /**
     * Updates the game.
     */
    fun update(): Boolean
}

/**
 * Repo-based game engine.
 */
class Wepwawet(mapper: Mapper) : Repo(mapper) {
    /**
     * A local loopback server.
     */
    class Loopback {
        /**
         * A client connected to the loopback server.
         */
        private class Client {
            /**
             * The send channel of the client
             */
            val sendChannel = Channel<Ctrl>(UNLIMITED)

            /**
             * The receive channel of the client
             */
            val receiveChannel = Channel<Ctrl>(UNLIMITED)

            /**
             * True if client accepted.
             */
            var isAccepted = false
        }

        /**
         * List of all connections.
         */
        private val connections = arrayListOf<Client>()

        init {
            launch(CommonPool) {
                // While server is running
                while (isActive) {
                    val cs = connections.toList()
                    // Handle all messages by all connections
                    for (c in cs) {
                        val x = c.receiveChannel.poll()

                        // Distinguish message type
                        when (x) {
                        // Distribute a lobby value
                            is LobbyValue ->
                                for (c2 in cs)
                                    c2.sendChannel.send(x)

                        // Handle client accept
                            is Accept ->
                                c.isAccepted = true

                        // Handle client decline
                            is Decline ->
                                c.isAccepted = true

                        // Handle client disconnect
                            is Disconnect ->
                                connections -= c

                        // Handle runtime dispatch
                            is SignOff, is Dispatch ->
                                for (c2 in cs)
                                    if (c != c2)
                                        c2.sendChannel.send(x)
                        }
                    }

                    // If all connected clients accepted, start
                    if (cs.isNotEmpty() && cs.all { it.isAccepted }) {
                        for (c in cs) {
                            for (i in 0..cs.size)
                                c.sendChannel.send(SignOff(Rev(0, 0, (i + 1).toByte())))
                        }

                        for ((i, c) in cs.withIndex()) {
                            c.sendChannel.send(Go((i + 1).toByte()))
                        }
                    }
                }
            }
        }

        /**
         * Opens a uplink/downlink pair.
         */
        fun open(): Pair<SendChannel<Ctrl>, ReceiveChannel<Ctrl>> = Client().let {
            connections += it
            it.receiveChannel to it.sendChannel
        }
    }

    /**
     * Message for lobby accept.
     */
    private class Accept : Ctrl

    /**
     * Message for lobby decline.
     */
    private class Decline : Ctrl

    /**
     * Message for client disconnect.
     */
    private class Disconnect : Ctrl

    /**
     * Message for lobby value exchange.
     */
    private class LobbyValue(val key: String, val value: Any?) : Ctrl

    /**
     * Message for lobby to game transition.
     */
    private class Go(val origin: Byte) : Ctrl

    /**
     * Message for revision sign-off.
     */
    private class SignOff(val rev: Rev) : Ctrl

    /**
     * Message for call dispatch.
     */
    private class Dispatch(val rev: Rev, val call: Call) : Ctrl

    /**
     * Connection container.
     */
    private class Connection(val send: SendChannel<Ctrl>, val receive: ReceiveChannel<Ctrl>)

    /**
     * The current connection.
     */
    private var connection: Connection? = null

    /**
     * The origin identity.
     */
    private var origin: Byte? = null


    /**
     * Internal store of sign off values.
     */
    private val signOffs = hashMapOf<Byte, Rev>()

    /**
     * Sends the control message safely.
     */
    private fun sendSafe(ctrl: Ctrl) {
        // Get connection
        val c = connection ?: throw IllegalStateException("Trying to send without a connection.")

        // Offer or run blocking send
        if (!c.send.offer(ctrl))
            runBlocking { c.send.send(ctrl) }
    }

    /**
     * Receives all messages until current end is reached.
     */
    private fun receiveSafeAll(): List<Ctrl> {
        // Get connection
        val c = connection ?: throw IllegalStateException("Trying to receive without a connection.")

        return generateSequence { c.receive.poll() }.toList()
    }

    override fun onRootImpulse(rev: Rev, call: Call) {
        // Intercept root impulse and dispatch.
        sendSafe(Dispatch(rev, call))
    }

    /**
     * Runs the game with [pregame] as the lobby routine and [game] as the game routine.
     */
    fun run(pregame: suspend Lobby.() -> Unit, game: suspend Game.() -> Unit) {
        // Create the main lobby object
        val l = object : Lobby {
            /**
             * True if go signal was received
             */
            private var isAccepted = false

            override val current = hashMapOf<String, Any?>()

            override var isInLobby: Boolean = true

            override fun update() {
                for (m in receiveSafeAll())
                    when (m) {
                    // Set lobby value to received value
                        is LobbyValue -> {
                            current[m.key] = m.value
                        }

                    // Set origin to received value and deactivate the repeat lock
                        is Go -> {
                            origin = m.origin
                            isInLobby = false
                        }
                    }
            }

            override fun send(key: String, value: Any?) {
                if (!isAccepted)
                    sendSafe(LobbyValue(key, value))
            }

            override fun accept() {
                isAccepted = true
                sendSafe(Accept())
            }

            override fun decline() {
                sendSafe(Decline())
                isAccepted = false
            }

            override fun connect(send: SendChannel<Ctrl>, receive: ReceiveChannel<Ctrl>) {
                connection = Connection(send, receive)
            }

            override fun disconnect() {
                sendSafe(Disconnect())
                connection = null
                isInLobby = false
            }
        }

        // Run the pregame
        runBlocking { l.pregame() }

        // When pregame is completed, it's map is the lobby assignment, it is then used by the game runner
        val g = object : Game {
            private var startedAt = System.currentTimeMillis()

            override fun <T : Entity> start(game: (Node, Map<String, Any?>) -> T): T {
                head = Rev(0, 0, 0)
                runId = 0

                val r = construct(game, l.current)

                head = Rev(0, 0, origin!!)
                runId = 0

                return r
            }

            var lu: Int? = null
            override fun update(): Boolean {
                val nu = (System.currentTimeMillis() - startedAt).toInt() / 50 * 50
                if (nu == lu)
                    return false

                lu = nu
                update(nu)
                return true
            }

        }

        // Run the game
        if (connection != null) {
            runBlocking { g.game() }
            sendSafe(Disconnect())
        }
    }


    /**
     * Updates to the next time.
     */
    private fun update(time: Int) {
        // Make next revision
        val next = if (head.major != time)
            head.setMajor(time)
        else
            head.incMinor()

        // Sign off will be this time
        signOffs[head.origin] = next

        // Make insert buffer
        val inserts = TreeMap<Rev, Call>()

        // Handle all incoming messages
        for (m in receiveSafeAll())
            when (m) {
                is SignOff -> signOffs[m.rev.origin] = m.rev
                is Dispatch -> inserts[m.rev] = m.call
            }

        // Insert received calls
        insert(inserts)

        // Determine sign-off cannot be empty.
        // TODO: Sign-off causes bugs with mismatching read semantics :(
        head = signOffs.values.min()!!
        head = head.copy(major = head.major - 1000)
        runId = 0

        signOff()

        // Move to next revision and send own sign-off value
        head = next
        runId = 0

        sendSafe(SignOff(head))
    }
}