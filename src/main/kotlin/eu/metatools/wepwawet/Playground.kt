package eu.metatools.wepwawet

import kotlin.properties.Delegates.notNull

class Y(container: Container) : Entity(container) {
    var i by key(Int.MAX_VALUE)

    var x by prop(0)

    val cmd by impulse { ->
        x *= 2
    }

    override fun toString() = "Y(id=${primaryKey()})[x=$x]"
}

class Root(container: Container) : Entity(container) {
    var children by prop(listOf<Y>())

    val cmd by impulse { ->
        children += create(::Y).apply {
            i = children.size
        }

        for (c in children)
            c.x += 1
    }
}

fun main(args: Array<String>) {
    var x by notNull<Container>()
    var y by notNull<Container>()

    x = object : Container(1) {
        override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
            y.receive(time, id, call, arg)
        }
    }

    y = object : Container(2) {
        override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
            x.receive(time, id, call, arg)
        }
    }

    val a = x.init(::Root)
    val b = y.init(::Root)

    b.cmd()
    println(a.children)
    println(b.children)


    a.cmd()
    println(a.children)
    println(b.children)

    x.time = 1
    a.cmd()
    println(a.children)
    println(b.children)

    y.time = 1
    b.children[0].cmd()
    println(a.children)
    println(b.children)

    println("-------")
    println(x.match<Y>(listOf(null)))

    x.repo.softUpper = Revision(0, 1)
    println(x.match<Y>(listOf(null)))

    x.repo.softUpper = null
    println(x.match<Y>(listOf(null)))

}