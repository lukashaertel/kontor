package eu.metatools.wepwawet

import eu.metatools.kontor.tools.randomOf
import kotlinx.coroutines.experimental.*
import java.util.concurrent.Semaphore

class Y(override val node: Node, x: X) : Entity {
    val x: X by prop(x)

    var y by prop(1)

    val cmd by impulse { ->
        y++
    }

    override fun deleting() {
        x.d -= y
    }

    override fun toString() = node.toString()
}

class X(override val node: Node, val lobby: Map<String, Any?>) : Entity {
    var a by prop(0)

    var b by prop(0)

    var c by prop(emptyList<Y>())

    var d by prop(0)

    override fun constructed() {
        println("Constructed on $lobby")
    }

    val x by impulse { i: Int ->
        if (a >= i) {
            b++
            a -= i
            c += construct(::Y, this)
        }
    }


    val y by impulse { ->
        a++
    }

    val z by impulse { ->
        if (c.isNotEmpty()) {
            val x = c.first()
            c -= x
            delete(x)
        }
    }

    val w by impulse { ->
        if (c.isNotEmpty())
            c.first().cmd()
    }

    override fun toString() = node.toString()
}

fun main(args: Array<String>) = runBlocking<Unit> {
    val ls = Wepwawet.Loopback()

    val mx = Mapper(setOf(X::class, Y::class))
    // Make both games
    val a = launchInstance(mx, ls, "a")
    val b = launchInstance(mx, ls, "b")
    val c = launchInstance(mx, ls, "c")

    readLine()

    a.cancel()
    b.cancel()
    c.cancel()
}

val sem = Semaphore(1, true)
private fun launchInstance(mx: Mapper, ls: Wepwawet.Loopback, id: String, printEvery: Int = 1000): Job {
    val (up1, down1) = ls.open()
    return launch(CommonPool) {
        val r = Wepwawet(mx)
        r.run({
            connect(up1, down1)
            accept()
            while (isInLobby) {
                update()
                delay(10)
            }
        }) {
            println("Starting game")
            val x = start(::X)
            var rn = 0

            fun printDebug() {
                // Print
                try {
                    sem.acquireUninterruptibly()
                    val printRev = Rev.upperBoundOf(r.head)
                    print(id)
                    print('@')
                    print(printRev)
                    print(" (RN=")
                    print(rn)
                    println("):")
                    println(x.node.toWideString(printRev))
                } finally {
                    sem.release()
                }
            }

            while (isActive) {
                // Update
                if (update()) {
                    rn++

                    printDebug()

                    // Action
                    when (randomOf("x", "y", "x", "w", "w", "y", "x", "y", "z")) {
                        "x" -> x.x(3)
                        "y" -> x.y()
                        "z" -> x.z()
                        "w" -> x.w()
                    }
                } else
                    yield()
            }

            printDebug()
        }
    }
}