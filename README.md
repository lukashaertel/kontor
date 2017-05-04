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

Once the username is entered, the chat client is properly configured, a repeated task is launched for all inbound messages, printing them.

```kotlin
    k.inbound.launchConsumer {
        it.applyIfIs<Message> {
            println("$username: $string")
        }
    }
```

Aside from that, a second task is lauched that reads the user's input (`consoleLines`) and feeds them into the outbound messages. Once there are no lines anymore, the client is stopped.

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

Upon connection, all messages in the history are sent to the new client.

```kotlin
    k.network.launchConsumer {
        when (it is Connected) {
            for (msg in history)
                it.channel.writeAndFlush(msg)
        }
    }

```

All incoming messages are sent to all clients except the sender, they are also backed in the history.

```kotlin
    k.inbound.launchConsumer { (msg, c) ->
        msg.applyIfIs<Message> {
            history += this
            println("$username: $string")
        }

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
