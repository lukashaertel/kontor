package eu.metatools.kontor

import eu.metatools.kontor.tools.await
import eu.metatools.kontor.tools.consoleLines
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    val k = KontorClient(Message::class)
    println("Connecting to server")

    await(k.start("localhost", 5000))

    print("Connected to server, enter username: ")
    val username = readLine()!!

    // Handling of messages
    launch(CommonPool) {
        k.inbound.consumeEach {
            if (it is Message) {
                print(it.username)
                print(": ")
                println(it.string)
            }
        }
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