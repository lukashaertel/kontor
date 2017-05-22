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
    var money by prop(100)
}

class Game(override val node: Node, val lobby: Lobby) : Entity {
    var variable by prop(0)

    val cmd by impulse { s: String ->
        when (s) {
            "i" -> variable += 1
            "d" -> if (variable > 0) variable -= 1
        }

        if (variable % 20 == 19)
            create(::Nimbus)
    }
}

fun main(args: Array<String>) = runBlocking {
    val a = AssociateMapper(Game::class, Nimbus::class)
    val n1 = DummyNet("Net 1", 0)
    val n2 = DummyNet("Net 2", 1000000, n1.outbound, n1.inbound)

    val w1 = Wepwawet(a, n1)
    val w2 = Wepwawet(a, n2)

    launch(CommonPool) {
        while (isActive) {
            w1.netSend()
            w1.netReceive()
        }
    }

    launch(CommonPool) {
        while (isActive) {
            w2.netSend()
            w2.netReceive()
        }
    }

    val l = HashLobby()

    // Simulate one computer
    val r1 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w1.start(l, ::Game)
        delay(120)

        launch(CommonPool) {
            while (isActive) {
                game.cmd(randomOf("i", "i", "d"))
                delay(75)
            }
        }

        while (isActive) {
            w1.update(System.currentTimeMillis() - s)
            delay(25)
        }

    }

    // Simulate another computer
    val r2 = launch(CommonPool) {
        val s = System.currentTimeMillis()
        val game = w2.start(l, ::Game)
        delay(120)

        launch(CommonPool) {
            while (isActive) {
                game.cmd(randomOf("i", "i", "d"))
                delay(125)
            }
        }

        while (isActive) {
            w2.update(System.currentTimeMillis() - s)
            delay(25)
        }
    }
    val reader = launch(CommonPool) {
        while (isActive) {
            try {
                println("W1")
                dumpET(w1.entityTable)

                println("W2")
                dumpET(w2.entityTable)


                delay(100)
            } catch(_: Throwable) {
            }
        }
    }

    r1.join()
    r2.join()
    reader.join()
}

fun dumpET(entityTable: EntityTable) {
    for ((i, e) in entityTable) {
        println("Entity $e (id=$i)")
        for (k in e.node.revTable.keys()) {
            println("  $k")
            for ((r, v) in e.node.revTable.getAll(k))
                println("    ${v.toString().padStart(10)}  @$r")
        }
    }
}