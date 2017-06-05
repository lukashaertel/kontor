package eu.metatools.wepwawet

import eu.metatools.kontor.tools.randomOf
import kotlinx.coroutines.experimental.*

class Y(override val node: Node, x: X) : RootEntity() {
    val x: X by prop(x)

    override fun constructed() {
        x.d++

        println("Ann yang")
    }

    override fun deleting() {
        x.d++

        println("Bye")
    }
}

class X(override val node: Node, val lobby: Map<String, Any?>) : RootEntity() {
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
            println("Plop")
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
}

fun main(args: Array<String>) = runBlocking {
    val ls = Wepwawet.Loopback()

    val mx = Mapper(setOf(X::class, Y::class))
    // Make both games
    val a = launchInstance(mx, ls)
    val b = launchInstance(mx, ls)

    a.join()
    b.join()
}

private fun launchInstance(mx: Mapper, ls: Wepwawet.Loopback): Job {
    val (up1, down1) = ls.open()
    val a = launch(CommonPool) {
        val r = Wepwawet(mx)
        r.run({
            connect(up1, down1)
            var x = 100
            while (isInLobby) {
                update()
                delay(50)

                if (x % 10 == 0)
                    send("a", randomOf("a", "b", "c"))

                if (x-- == 0)
                    accept()

                println(current)
            }
        }) {
            println("Starting game")
            val x = start(::X)
            while (true) {
                update()
                delay(50)
                println(x)
            }
        }
    }
    return a
}