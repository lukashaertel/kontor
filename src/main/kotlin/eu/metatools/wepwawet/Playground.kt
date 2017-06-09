package eu.metatools.wepwawet

import com.google.common.collect.ComparisonChain
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.Terminal
import eu.metatools.common.randomOf
import eu.metatools.common.randomTrue
import eu.metatools.common.sTerminal
import eu.metatools.common.sText
import kotlinx.coroutines.experimental.*
import java.lang.Math.round
import kotlin.properties.Delegates.notNull

class Y(container: Container) : Entity(container) {
    var i by key(Int.MAX_VALUE)

    var x by prop(0)

    val cmd by impulse { ->
        x /= 2
    }

    override fun toStringMembers() = "i=$i, x=$x"
}

class Root(container: Container) : Entity(container, AutoKeyMode.PER_CLASS) {
    var children by prop(listOf<Y>())

    val cmd by impulse { arg: String ->
        when (arg) {
            "add" -> children += create(::Y).apply {
                i = children.size
            }

            "inc" -> for (c in children)
                c.x += 1

            "del" -> if (children.isNotEmpty()) {
                children -= children.last().also {
                    delete(it)
                }
            }
        }
    }

    override fun toStringMembers() = "size=${children.size}"
}

fun <T> Map<List<Any>, T>.softSort() = entries.sortedWith(Comparator { xs, ys ->
    val c = ComparisonChain.start()
    c.compare(xs.key.size, ys.key.size)
    for ((x, y) in xs.key zip ys.key) {
        val xc = x.javaClass
        val yc = y.javaClass
        c.compare(xc.name, yc.name)
        if (x is Comparable<*> && y is Comparable<*>)
            c.compare(x, y)
    }
    c.result()
})


// Make synchronized access variable
val access = Any()

fun TextGraphics.playGame(terminal: Terminal, container: Container, root: Root) = launch(CommonPool) {
    val s = System.currentTimeMillis()

    var runs = 0
    while (isActive) {
        synchronized(access) {
            container.apply {
                time = (System.currentTimeMillis() - s).toInt()
                repo.softUpper = rev()

                // Get width and height of output
                val width = terminal.terminalSize.columns / 2
                val height = terminal.terminalSize.rows
                val col = container.author * width

                // Get clear stirng
                val clear = " ".repeat(width)


                for (i in 0..height)
                    putString(col, i, clear)

                putString(col, 0, "Author: $author")
                putString(col, 1, "Time: $time")
                putString(col, 2, "FPS: ${round(1000.0 * runs / time)}")

                for ((i, e) in index.softSort().withIndex()) {
                    putString(col + 2, 7 + i, "${e.key}:")
                    putString(col + 12, 7 + i, "${e.value}")
                }

                if (randomTrue(.60))
                    root.cmd(randomOf("add", "inc", "del").also {
                        putString(col, 4, "Root: cmd($it)")
                    })

                if (randomTrue(.25))
                    if (root.children.isNotEmpty()) {
                        root.children.first().cmd()
                        putString(col, 5, "First child: cmd()")
                    }

                terminal.flush()
            }
        }

        runs++
        yield()
    }
}

fun main(args: Array<String>) = runBlocking {
    sTerminal {
        addResizeListener { _, _ -> clearScreen() }
        setCursorVisible(false)
        sText {
            // Create the container locations
            var x by notNull<Container>()
            var y by notNull<Container>()

            // Construct the containers
            x = object : Container(0) {
                override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
                    y.receive(time, id, call, arg)
                }
            }

            y = object : Container(1) {
                override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
                    x.receive(time, id, call, arg)
                }
            }

            // Initialize
            val a = x.init(::Root)
            val b = y.init(::Root)

            val j1 = playGame(this@sTerminal, x, a)
            val j2 = playGame(this@sTerminal, y, b)

            while (readInput().keyType != KeyType.Escape) {
            }
            println("Canceling")
            j1.cancel()
            j2.cancel()
            j1.join()
            j2.join()

            while (readInput().keyType != KeyType.Escape) {
            }
        }
    }
}