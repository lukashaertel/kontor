package eu.metatools.kontor

import eu.metatools.kontor.server.Connected
import eu.metatools.kontor.server.Disconnected
import eu.metatools.kontor.tools.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.serialization.Serializable

@Serializable
data class Message(val username: String, val string: String)

fun main(args: Array<String>) = runBlocking {
    val k = KontorServer(Message::class)
    println("Starting server")

    await(k.start(5000))

    print("Started server, enter username: ")
    val username = readLine()!!
    val history = arrayListOf<Message>()

    // Network management messages
    k.network.launchConsumer {
        when (it) {
            is Connected -> {
                for (msg in history)
                    it.channel.writeAndFlush(msg)
                println("Connected: ${it.channel.remoteAddress()}")
            }
            is Disconnected -> {
                println("Disconnected: ${it.channel.remoteAddress()}")
            }
        }
    }

    // Handling of messages
    k.inbound.launchConsumer { (msg, c) ->
        msg.applyIfIs<Message> {
            history += this
            println("$username: $string")
        }

        // Loopback any message
        k.outbound.sendAllExcept(msg, c)
    }


    // User input
    for (s in consoleLines)
        Message(username, s).apply {
            history += this
            k.outbound.sendAll(this)
        }

    println("Stopping server")

    await(k.stop())

    println("Terminating workers")

    k.shutdown().join()
}