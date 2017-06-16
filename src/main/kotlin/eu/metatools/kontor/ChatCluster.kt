package eu.metatools.kontor

import eu.metatools.common.consoleLines
import eu.metatools.common.pick
import eu.metatools.kontor.tools.sendAll
import eu.metatools.kontor.tools.toProsumer
import kotlinx.coroutines.experimental.runBlocking
import org.jgroups.Address
import kotlin.serialization.Serializable

@Serializable
data class History(val messages: List<Message> = listOf()) {
    operator fun plus(message: Message) =
            History(messages + message)
}

/**
 * History of the chatty room.
 */
var history = History()

fun main(args: Array<String>) = runBlocking {
    val k = KontorCluster(::history.toProsumer(), History::class, Message::class)
    println("Joining cluster")

    k.start("Chatty").join()

    print("Joined cluster, enter username: ")
    val username = readLine()!!

    // Network management messages are not handled
    k.management.close()

    // Handling of messages
    k.inbound pick { (msg, _): From<Message, Address> ->
        history += msg
        println(msg)
    }

    // Request message history
    k.requestState()
    for (msg in history.messages)
        println(msg)

    // User input
    for (s in consoleLines)
        k.outbound.sendAll(Message(username, s))


    println("Stopping server")

    k.stop().join()

    println("Terminating workers")

    k.shutdown().join()
}