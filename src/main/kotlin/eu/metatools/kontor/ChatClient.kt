package eu.metatools.kontor

import eu.metatools.common.consoleLines
import eu.metatools.common.pick
import eu.metatools.kontor.tools.await
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking {
    val k = KontorClient(Message::class)
    println("Connecting to server")

    await(k.start("localhost", 5000))

    print("Connected to server, enter username: ")
    val username = readLine()!!

    // Handling of messages
    k.inbound pick { msg: Message ->
        println(msg)
    }

    // Handling of input
    launch(CommonPool) {
        // User input
        for (s in consoleLines)
            k.outbound.send(Message(username, s))

        println("Disconnecting from server")

        // Stop from user input
        k.stop()
    }

    // Handling of a remote termination
    await(k.disconnect())

    println("Terminating workers")

    k.shutdown().join()
}