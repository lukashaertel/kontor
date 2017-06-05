package eu.metatools.wepwawet

import eu.metatools.wepwawet.delegates.dynamicOf
import eu.metatools.wepwawet.delegates.impulse
import eu.metatools.wepwawet.delegates.reactTo
import eu.metatools.wepwawet.components.MapRevisionTable
import eu.metatools.wepwawet.net.Net
import kotlinx.coroutines.experimental.channels.Channel
import java.lang.Math.sqrt


class Test(
        override val parent: Wepwawet,
        override val id: Int) : Entity {
    // Statics and dynamics just need to be tracked locally
    var y by dynamicOf(100)

    var z by dynamicOf(2)

    var sum = 0

    var ysqrt = 0

    // Impulses need to be transferred with their parameters
    val otherMutate by impulse { i: Int ->
        z -= i
    }

    val mutate by impulse { i: Int ->
        y += i * z
        otherMutate(1)
    }

    // Updates need to be initialized and triggered on changes
    val updateSum by reactTo(Test::y, Test::z) {
        sum = y + z
    }

    val updateOverdeclared by reactTo(Test::y, Test::z) {
        ysqrt = sqrt(y.toDouble()).toInt()
    }
    val updateUnderdeclared by reactTo() {
        ysqrt = sqrt(y.toDouble()).toInt()
    }
}

fun main(args: Array<String>) {
    val w = Wepwawet(MapRevisionTable(), hashMapOf(), Net("player", Channel(), Channel()))

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