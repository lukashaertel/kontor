package eu.metatools.kontor

import eu.metatools.common.choose
import eu.metatools.common.consoleLines
import eu.metatools.common.pick
import eu.metatools.kontor.server.Connected
import eu.metatools.kontor.server.Disconnected
import kotlinx.coroutines.experimental.runBlocking
import rice.p2p.commonapi.NodeHandle
import java.net.InetAddress

fun main(args: Array<String>) = runBlocking {
    val k = KontorBootstrap(Message::class)
    println("Connecting to ring")

    k.start("localhost", 5000, "chat").join()

    print("Connected to ring, enter username: ")
    val username = readLine()!!

    // Handling of messages
    k.inbound pick { (n, s): Message ->
        println("$n: $s")
    }

    k.network choose { c: Connected<NodeHandle> ->
        println("Connected: ${c.channel.id}")
    } pick { d: Disconnected<NodeHandle> ->
        println("Disconnected: ${d.channel.id}")
    }

    // User input
    for (s in consoleLines)
        k.outbound.send(Message(username, s))

    println("Disconnecting from ring")

    // Disconnect
    k.stop().join()
}