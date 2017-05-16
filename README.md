# Kontor
A network server and client library supporting arbitrary message types and generalized inbound/outbound channels. This project demonstrates some experimental Kotlin features, namely [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) and the [Serialization Prototype](https://github.com/elizarov/KotlinSerializationPrototypePlayground).

## Entry points
See an exampel chat [client](https://github.com/lukashaertel/kontor/blob/master/src/main/kotlin/eu/metatools/kontor/ChatClient.kt) and [server](https://github.com/lukashaertel/kontor/blob/master/src/main/kotlin/eu/metatools/kontor/ChaServer.kt) that support history of chat messages. An abridged demo is given below.

## Building
The project should build out of the box. To run properly in IntelliJ, edit the run configuration of the respective main and replace the Kotlin build with a call to the gradle task build. This is neccessary since this project utilizes the serialization compiler plugin.

![IntelliJ run configuration](https://github.com/lukashaertel/kontor/raw/wsdata/config.png)

## Other takeaways
This project lays out how to:
* bridge [Netty](http://netty.io/) and Kotlin coroutines.
* bridge Netty and the Serialization Prototype.



## Client example
A client is initialized on the classes it may serialize. It is connected ot the server on the local machine, upon connection, the user is promted for a username.

```kotlin
fun main(args: Array<String>) = runBlocking {
    val k = KontorClient(Message::class)
    await(k.start("localhost", 5000))

    print("Connected to server, enter username: ")
    val username = readLine()!!

```

Once the username is entered, the chat client is properly configured, a repeated task is launched that consumes a channel, picking only Messages and printing them.

```kotlin
   k.inbound pick { (n, s): Message ->
        println("$n: $s")
    }
```

Aside from that, a second task is lauched that backingReads the user's input (`consoleLines`) and feeds them into the outbound messages. Once there are no lines anymore, the client is stopped.

```kotlin
    launch(CommonPool) {
        for (s in consoleLines)
            k.outbound.send(Message(username, s))
        k.stop()
    }
```

With both tasks running, the main block awaits disconnection of the client and then shuts down the workers.

```kotlin
    await(k.disconnect())
    k.shutdown().join()
```

## Server example 
A data class is specified for the messages, it is serializable and appropriate methods are generated through the serialization framework.

```kotlin
@Serializable
data class Message(val username: String, val string: String)
```

The server is started and awaits connections on the given port. The username is also prompted and a history of chat messages is initialized.

```kotlin
fun main(args: Array<String>) = runBlocking {
    val k = KontorServer(Message::class)
    await(k.start(5000))

    print("Started server, enter username: ")
    val username = readLine()!!
    val history = arrayListOf<Message>()

```

From the network management channel, all `Connected` messages are chosen and handled by sending the new client the existing history. From the remaining network management messages, `Disconnected` is logged.

```kotlin
    k.network choose { c: Connected ->
        for (msg in history)
            c.channel.writeAndFlush(msg)
        println("Connected: ${c.channel.remoteAddress()}")
    } pick { d: Disconnected ->
        println("Disconnected: ${d.channel.remoteAddress()}")
    }
```

All incoming messages are sent to all clients except the sender, they are also backed in the history.

```kotlin
    k.inbound pick { (msg, c): From<Message> ->
        history += msg
        println("${msg.username}: ${msg.string}")

        // Loopback any message
        k.outbound.sendAllExcept(msg, c)
    }
```

The server itself can also chat. Messages are added to the histroy and sent to all clients. When no more lines are available (an empty line was entered), the server terminates.

```kotlin
    for (s in consoleLines)
        Message(username, s).apply {
            history += this
            k.outbound.sendAll(this)
        }

    await(k.stop())
    k.shutdown().join()
}
```
