package eu.metatools.wepwawet

import eu.metatools.wepwawet.delegates.dynamicOf
import eu.metatools.wepwawet.delegates.impulse
import eu.metatools.wepwawet.delegates.reactTo
import eu.metatools.wepwawet.components.MapRevisionTable
import eu.metatools.wepwawet.net.Net
import kotlinx.coroutines.experimental.channels.Channel


class Test(
        override val parent: Wepwawet,
        override val id: Int) : Entity {
    var y by dynamicOf(100)

    var z by dynamicOf(2)

    var sum = 0

    val otherMutate by impulse { i: Int ->
        z -= i
    }

    val mutate by impulse { i: Int ->
        y += i * z
        otherMutate(1)
    }

    val updateSum by reactTo(this::y, this::z) {
        sum = y + z
    }
}

fun main(args: Array<String>) {
    var rid = 0
    val w = Wepwawet(MapRevisionTable(), Net("player", Channel(), Channel()))

    val test1 = w.obtain(::Test)
    val test2 = w.obtain(::Test)


    test2.mutate(12)

    w.time = 1
    w.simulateImpulse {
        test1.y = 400
    }
    w.time = 2


    w.simulateImpulse {
        test2.z = 234
    }

    w.stats()
}