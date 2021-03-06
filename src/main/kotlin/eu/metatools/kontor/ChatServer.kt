package eu.metatools.kontor

import eu.metatools.common.choose
import eu.metatools.common.consoleLines
import eu.metatools.common.pick
import eu.metatools.kontor.tools.await
import eu.metatools.kontor.tools.sendAll
import eu.metatools.kontor.tools.sendAllExcept
import io.netty.channel.Channel
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class Message(val username: String, val string: String) {
    override fun toString() = "$username: $string"
}

fun main(args: Array<String>) = runBlocking {
    val k = KontorServer(Message::class)
    println("Starting server")

    await(k.start(5000))

    print("Started server, enter username: ")
    val username = readLine()!!
    val history = arrayListOf<Message>()

    // Network management messages
    k.management choose { c: Connected<Channel> ->
        for (msg in history)
            c.channel.writeAndFlush(msg)
        println("Connected: ${c.channel.remoteAddress()}")
    } pick { d: Disconnected<Channel> ->
        println("Disconnected: ${d.channel.remoteAddress()}")
    }

    // Handling of messages
    k.inbound pick { (msg, c): From<Message, Channel> ->
        history += msg
        println(msg)

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