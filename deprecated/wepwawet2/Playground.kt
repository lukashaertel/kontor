package eu.metatools.wepwawet2

import eu.metatools.kontor.tools.randomOf
import eu.metatools.wepwawet2.components.*
import eu.metatools.wepwawet2.data.Lobby
import eu.metatools.wepwawet2.dsls.*
import eu.metatools.wepwawet2.util.Add
import eu.metatools.wepwawet2.util.Remove
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.PrintStream
import java.util.concurrent.TimeUnit

class Nimbus(override val node: Node) : Entity {
    var seen by prop(0)

    override fun toString() = "Nimbus[${node.id}]"
}

class Game(override val node: Node, val lobby: Lobby) : Entity {
    var money by prop(0)

    var nimbi by prop(emptyList<Nimbus>())

    val cmd by impulse { s: String ->
        when (s) {
            "i" -> money += 1
            "d" -> if (money >= 10) {
                money -= 10
                nimbi += create(::Nimbus)
            }
            "u" -> for (n in nimbi)
                n.seen += 1
            "r" -> {
                val n = nimbi.firstOrNull()
                if (n != null) {
                    nimbi -= n
                    evict(n)
                }
            }
        }
    }

    override fun toString() = "Game[${node.id}]"

}

val console = Any()
val dumpRev = false
val dumpTT = false
val dumpET = false
fun Wepwawet.dump(printStream: PrintStream) {
    synchronized(console) {
        printStream.apply {
            if (dumpRev)
                println("Rev $now")
            if (dumpTT) {
                println("Tracking table:")
                for ((k, v) in trackingTable.asMap().descendingMap().entries.take(10).asReversed())
                    println("  $k -> $v")
            }

            if (dumpET) {
                println("Entities:")
                println("  active")
                for ((id, m) in entityTable.backing.asMap().filter { it.value.size == 1 }) {
                    val (x) = m.entries.toList()

                    if (x.value is Add<*>)
                        println("    $id from ${x.key}: ${x.value.element}")
                    else if (x.value is Remove<*>)
                        println("    $id to ${x.key}")
                }
                println("  legacy")
                for ((id, m) in entityTable.backing.asMap().filter { it.value.size == 2 }) {
                    val (x, y) = m.entries.toList()
                    println("    $id from ${x.key} to ${y.key}: ${x.value.element}")
                }
            }
            // TODO: This disregards multiple occupation, which is rare.
            println()
        }
    }
}

val simTime = 10
fun main(args: Array<String>) = runBlocking {
    val a = AssociateMapper(Game::class, Nimbus::class)
    val n1 = DummyNet("Net 1")
    val n2 = DummyNet("Net 2", n1.outbound, n1.inbound)

    val ps1 = PrintStream("w1")// PrintStream(multiplex(System.out, PrintStream("w1")))
    val ps2 = PrintStream("w2")//PrintStream(multiplex(System.out, PrintStream("w2")))


    val w1 = Wepwawet(a, n1)
    val w2 = Wepwawet(a, n2)

    val l = Lobby()

    // Simulate one computer
    val r1 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w1.start(0, l, ::Game)

        while (isActive) {
            w1.update((System.currentTimeMillis() - s).toInt())
            game.cmd(randomOf("i", "i", "d", "d", "i", "d", "d", "d", "u", "d", "u", "r"))
            w1.dump(ps1)
        }

    }

    // Simulate another computer
    val r2 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w2.start(1, l, ::Game)

        while (isActive) {
            w2.update((System.currentTimeMillis() - s).toInt())
            game.cmd(randomOf("i", "i", "i", "i", "d", "d", "d", "d", "u", "d", "d", "u", "r"))
            w2.dump(ps2)
        }
    }

    launch(CommonPool) {
        delay(simTime.toLong(), TimeUnit.SECONDS)
        r1.cancel()
        r2.cancel()
    }
    r1.join()
    r2.join()
}