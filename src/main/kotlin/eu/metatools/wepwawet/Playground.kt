package eu.metatools.wepwawet


class Test<I>(
        override val parent: Wepwawet<I>,
        override val id: I) : Entity<I> {
    var y by dynamicOf(100)

    var z by dynamicOf(100)

    val mutate by action {
        y += 100
    }
}

fun main(args: Array<String>) {
    val w = Wepwawet(MapRegistry<Int>())

    val test1 = Test(w, 1)
    val test2 = Test(w, 2)

    test2.mutate()

    w.time = 1

    test1.y = 400

    w.time = 2


    test2.z = 234

    w.stats()

    w.stats()
}