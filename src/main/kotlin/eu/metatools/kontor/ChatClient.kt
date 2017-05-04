package eu.metatools.kontor

import eu.metatools.kontor.tools.await
import kotlinx.coroutines.experimental.CommonPool
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
        for (msg in k.inbound)
            if (msg is Message) {
                print(msg.username)
                print(": ")
                println(msg.string)
            }
    }

    // Handling of input
    launch(CommonPool) {
        // User input
        for (s in generateSequence(::readLine).takeWhile(String::isNotEmpty))
            k.outbound.send(Message(username, s))

        println("Disconnecting from server")

        // Stop from user input
        k.stop()
    }

    // Handling of a remote termination
    await(k.disconnect())


    println("Terminating workers")
    val j = k.shutdown()
    println("Waiting for termination")
    await(j)
    println("Gracefully shut down")
}