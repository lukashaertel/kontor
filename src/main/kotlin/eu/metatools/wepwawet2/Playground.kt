package eu.metatools.wepwawet2

import eu.metatools.kontor.tools.randomOf
import eu.metatools.wepwawet2.components.AssociateMapper
import eu.metatools.wepwawet2.components.DummyNet
import eu.metatools.wepwawet2.components.HashLobby
import eu.metatools.wepwawet2.dsls.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class Nimbus(override val node: Node) : Entity {
    var seen by prop(0)
}

class Game(override val node: Node, val lobby: Lobby) : Entity {
    var money by prop(0)

    var nimbi by refs(emptyList<Nimbus>())

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
}

fun main(args: Array<String>) = runBlocking {
    val a = AssociateMapper(Game::class, Nimbus::class)
    val n1 = DummyNet("Net 1", 0)
    val n2 = DummyNet("Net 2", 100, n1.outbound, n1.inbound)

    val w1 = Wepwawet(a, n1)
    val w2 = Wepwawet(a, n2)

    val l = HashLobby()

    val console = Any()
    // Simulate one computer
    val r1 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w1.start(l, ::Game)

        var everyI = 0
        while (isActive) {
            w1.update(System.currentTimeMillis() - s)
            if (everyI++ == 6) {
                game.cmd(randomOf("i", "i", "d", "d", "i","d", "d", "d",  "u", "d", "u", "r"))

                println("W1")
                dumpET(w1.entityTable)

                everyI = 0
            }
            delay(25)
        }

    }

    // Simulate another computer
    val r2 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w2.start(l, ::Game)

        var everyI = 0
        while (isActive) {
            w2.update(System.currentTimeMillis() - s)
            if (everyI++ == 3) {
                game.cmd(randomOf("i", "i", "i", "i","d", "d", "d", "d",  "u", "d", "d", "u", "r"))

                println("W2")
                dumpET(w2.entityTable)
                everyI = 0
            }
            delay(60)
        }
    }

    r1.join()
    r2.join()
}

var short = false
fun dumpET(entityTable: EntityTable) {
    if (short)
        println(entityTable.keys.sorted().joinToString { it.toString().padStart(4) })
    else
        for ((i, e) in entityTable) {
            println("Entity $e (id=$i)")
            for (k in e.node.revTable.keys()) {
                print("  $k=")
                println(e.node.revTable.getAll(k).entries.joinToString { (r, v) -> "${v.toString().padStart(4)}@$r" })
            }
        }
}