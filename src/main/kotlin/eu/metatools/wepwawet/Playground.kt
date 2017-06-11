package eu.metatools.wepwawet

import com.google.common.collect.ComparisonChain
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.GridLayout.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import eu.metatools.common.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.experimental.channels.actor
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
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

    var ct by prop(0)

    val clear by impulse { ->
        ct += 1
        for (c in children)
            delete(c)
        children = listOf()
    }

    val cmd by impulse { arg: String ->
        ct += 1
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

    override fun toStringMembers() = "size=${children.size}, ct=$ct"
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


var pause = false

data class CallComponents(
        val revision: Revision,
        val id: List<Any>,
        val call: Byte,
        val arg: Any?
)

fun playGame(gui: MultiWindowTextGUI, container: Container, calls: Channel<CallComponents>, root: Root)
        : Pair<Panel, Job> {
    val s = System.currentTimeMillis()

    var timeLabel by notNull<Label>()
    var entityTable by notNull<Table<Any>>()
    var cmdTable by notNull<Table<Any>>()

    val result = Panel().apply {
        layoutManager = GridLayout(2)
        addComponent(Label("Time"))
        addComponent(Label("...").also { timeLabel = it })

        entityTable = Table<Any>("Key", "Values").apply {
            visibleRows = 50
        }
        cmdTable = Table<Any>("Target", "Cmd").apply {
            visibleRows = 50
        }

        if (container.author.rem(2) == 0) {
            addComponent(cmdTable)
            addComponent(entityTable)
        } else {
            addComponent(entityTable)
            addComponent(cmdTable)
        }
    }

    val job = launch(CommonPool) {
        while (isActive) {
            if (pause) {
                yield()
                continue
            }

            container.apply {
                for ((r, i, c, a) in generateSequence { calls.poll() })
                    receive(r, i, c, a)

                time = (System.currentTimeMillis() - s).toInt()
                repo.softUpper = rev()
                repo.drop(Revision(time - 60 * 1000, 0, 0))


                val minutes = (time / 1000 / 60).toString().padStart(2, '0')
                val seconds = (time / 1000).rem(60).toString().padStart(2, '0')
                val millis = time.rem(1000).toString().padStart(3, '0')

                timeLabel.text = "$minutes:$seconds.$millis"

                gui.guiThread.invokeAndWait {
                    entityTable.tableModel.apply {
                        while (rowCount > 0)
                            removeRow(0)
                        for ((k, v) in index.softSort())
                            addRow(k, v.toStringMembers())
                    }
                }


                cmdTable.tableModel.apply {
                    if (randomTrue(.60))
                        root.cmd(randomOf("add", "add", "add", "inc", "inc", "del").also {
                            gui.guiThread.invokeAndWait {
                                insertRow(0, listOf("Root", "cmd($it)"))
                            }
                        })

                    if (randomTrue(.25))
                        if (root.children.isNotEmpty()) {
                            root.children.first().cmd()
                            gui.guiThread.invokeAndWait {
                                insertRow(0, listOf("Child#1", "cmd()"))
                            }
                        }

                    if (randomTrue(.0025)) {
                        root.clear()
                        gui.guiThread.invokeAndWait {
                            insertRow(0, listOf("Root", "clear()"))
                        }
                    }

                    gui.guiThread.invokeAndWait {
                        while (rowCount > 100)
                            removeRow(100)
                    }
                }
            }

            delay(20)
        }
    }
    return result to job
}

val pingMin = 30
val pingMax = 500

fun main(args: Array<String>) = runBlocking {
    term {
        //TODO Some problem where game gets locked after clear
        // Create the container locations
        var x by notNull<Container>()
        var y by notNull<Container>()

        val toX = Channel<CallComponents>(UNLIMITED)
        val toY = Channel<CallComponents>(UNLIMITED)

        val randomLatency = (pingMax..pingMax).toList().toTypedArray()
        // Construct the containers
        x = object : Container(0) {
            override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
                launch(CommonPool) {
                    delay(randomOf(*randomLatency).toLong())
                    toY.send(CallComponents(time, id, call, arg))
                }
            }
        }

        y = object : Container(1) {
            override fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
                launch(CommonPool) {
                    delay(randomOf(*randomLatency).toLong())
                    toX.send(CallComponents(time, id, call, arg))
                }
            }
        }

        // Initialize
        val a = x.init(::Root)
        val b = y.init(::Root)
        val gui = MultiWindowTextGUI(this, DefaultWindowManager(), EmptySpace(TextColor.ANSI.WHITE))


        val xw = playGame(gui, x, toX, a)
        val yw = playGame(gui, y, toY, b)
        val window = BasicWindow().apply {
            setHints(setOf(Window.Hint.FULL_SCREEN))
            component = Panel().apply {
                layoutManager = GridLayout(2)
                addComponent(Button("pause") { pause = true })
                addComponent(Button("play") { pause = false })
                addComponent(xw.first)
                addComponent(yw.first)

            }
        }
        gui.addWindowAndWait(window)
        xw.second.cancel()
        yw.second.cancel()
    }
}