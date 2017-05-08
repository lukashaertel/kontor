package eu.metatools.wepwawet


class Test(
        override val parent: Wepwawet<Int>,
        override val id: Int) : Entity<Int> {
    var y by dynamicOf(100)

    var z by dynamicOf(2)

    val mutate by impulse { i: Int ->
        y += i * z
    }
}

fun main(args: Array<String>) {
    var rid = 0
    val w = Wepwawet(MapRegistry<Int>(), { rid++ })

    val test1 = w.obtain(::Test)
    val test2 = w.obtain(::Test)


    test2.mutate(readLine()?.toInt() ?: 0)

    w.time = 1

    test1.y = 400

    w.time = 2


    test2.z = 234

    w.stats()
}