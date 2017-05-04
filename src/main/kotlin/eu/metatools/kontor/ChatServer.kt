package eu.metatools.kontor

import eu.metatools.kontor.tools.await
import kotlinx.coroutines.experimental.CommonPool
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
    launch(CommonPool) {
        for (m in k.network) {
            if (m is Connected)
                for (msg in history)
                    m.channel.writeAndFlush(msg)

            println("<< $m >>")
        }
    }

    // Handling of messages
    launch(CommonPool) {
        for ((c, msg) in k.inbound) {
            if (msg is Message) {
                history += msg

                print(msg.username)
                print(": ")
                println(msg.string)
            }

            // Loopback any message
            k.outboundInvDesignated.send(c to msg)
        }
    }

    // User input
    for (s in generateSequence(::readLine).takeWhile(String::isNotEmpty)) {
        val msg = Message(username, s)
        history += msg
        k.outbound.send(msg)
    }

    println("Stopping server")

    await(k.stop())


    println("Terminating workers")
    val j = k.shutdown()
    println("Waiting for termination")
    j.join()
    println("Gracefully shut down")
}