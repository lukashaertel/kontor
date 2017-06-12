package eu.metatools.wepwawet

import com.google.common.collect.ComparisonChain
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import eu.metatools.common.*
import eu.metatools.wepwawet.tools.IndexFunction0
import eu.metatools.wepwawet.tools.Recorder
import eu.metatools.wepwawet.tools.recordFrom
import eu.metatools.wepwawet.tools.recorder
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.Channel.Factory.UNLIMITED
import java.util.*
import kotlin.properties.Delegates.notNull


class Y(container: Container, i: Int) : Entity(container) {
    var i by key(i)

    val xRecorder = recorder<Float>(3000)

    var x by prop(0, xRecorder::recordFrom)

    val cmd: IndexFunction0<Double, Unit> by impulse { ->
        x /= 2
        cmd[0.25]()
    }

    override fun toStringMembers() = "i=$i, x=$x(i=${xRecorder.exin(container.time)})"
}

class Root(container: Container) : Entity(container, AutoKeyMode.PER_CLASS) {
    var children by holdMany<Y>()

    var ct by prop(0)

    val clear by impulse { ->
        children = listOf()
    }

    val cmd by impulse { arg: String ->
        ct += 1
        when (arg) {
            "add" -> children += create(::Y, children.size).also {
                it.cmd()
            }

            "inc" -> for (c in children)
                c.x += 1

            "del" -> if (children.isNotEmpty()) {
                children -= children.last()
            }
        }
    }

    override fun toStringMembers() = "size=${children.size}, ct=$ct"
}

fun <T> Map<List<Any?>, T>.softSort() = entries.sortedWith(Comparator { xs, ys ->
    val c = ComparisonChain.start()
    c.compare(xs.key.size, ys.key.size)
    for ((x, y) in xs.key zip ys.key) {
        if (x == null && y == null)
            c.compare(0, 0)
        else if (x == null)
            c.compare(0, 1)
        else if (y == null)
            c.compare(1, 0)
        else {
            val xc = x.javaClass
            val yc = y.javaClass
            c.compare(xc.name, yc.name)
            if (x is Comparable<*> && y is Comparable<*>)
                c.compare(x, y)
        }
    }
    c.result()
})


var pause = false

data class CallComponents(
        val revision: Revision,
        val id: List<Any?>,
        val call: Byte,
        val arg: Any?
)

var allContainers by notNull<List<Container>>()

val simulatePing = false
val pingMin = 15
val pingMax = 75

fun playGame(gui: MultiWindowTextGUI, container: Container, calls: Channel<CallComponents>, root: Root)
        : Pair<Panel, Job> {
    val s = System.currentTimeMillis()

    var timeLabel by notNull<Label>()
    var apmLabel by notNull<Label>()
    var tasLabel by notNull<Label>()
    var entityTable by notNull<Table<Any>>()
    var cmdTable by notNull<Table<Any>>()

    val result = Panel().apply {
        layoutManager = GridLayout(2)
        addComponent(Label("Time"))
        addComponent(Label("...").also { timeLabel = it })
        addComponent(Label("APM"))
        addComponent(Label("...").also { apmLabel = it })
        addComponent(Button("Clear now") {
            root.clear()
        })
        addComponent(Button("Clear in 3 seconds") {
            root.clear[3.0]()
        })
        addComponent(Label("#TA"))
        addComponent(Label("...").also { tasLabel = it })

        entityTable = Table<Any>("Key", "Values")
        cmdTable = Table<Any>("Target", "Cmd")

        addComponent(cmdTable)
        addComponent(entityTable)
    }

    val job = launch(CommonPool) {
        var ac = 0
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


                val minutes = (time / 1000 / 60).toString().padStart(2, '0')
                val seconds = (time / 1000).rem(60).toString().padStart(2, '0')
                val millis = time.rem(1000).toString().padStart(3, '0')

                timeLabel.text = "$minutes:$seconds.$millis"
                apmLabel.text = "${1000 * 60 * ac / time}"

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
                        root.cmd(randomOf("add", "add", "inc", "inc", "inc", "del").also {
                            gui.guiThread.invokeAndWait {
                                insertRow(0, listOf("Root", "cmd($it)"))
                                ac++
                            }
                        })

//                    if (randomTrue(.25))
//                        if (root.children.isNotEmpty()) {
//                            val c = randomOf(*root.children.toTypedArray())
//                            c.cmd()
//                            gui.guiThread.invokeAndWait {
//                                insertRow(0, listOf("${c.primaryKey()}", "cmd()"))
//                                ac++
//                            }
//                        }


                    gui.guiThread.invokeAndWait {
                        while (rowCount > 100)
                            removeRow(100)
                    }
                }


                val mr = allContainers.map(Container::rev).min()!!
                val dr = Revision(mr.time - (pingMax * 2), 0, 0)
                repo.drop(dr)


                tasLabel.text = "${repo.revisions.size}"
            }

            (result.parent.size.rows - 6).let {
                entityTable.visibleRows = it
                cmdTable.visibleRows = it
            }

           delay(200)
        }
    }
    return result to job
}

fun main(args: Array<String>) = runBlocking {
    term {
        // Create the container locations
        var x by notNull<Container>()
        var y by notNull<Container>()

        val toX = Channel<CallComponents>(UNLIMITED)
        val toY = Channel<CallComponents>(UNLIMITED)

        val randomLatency = (pingMin..pingMax).toList().toTypedArray()
        // Construct the containers
        x = object : Container(0) {
            override fun dispatch(time: Revision, id: List<Any?>, call: Byte, arg: Any?) {
                if (simulatePing)
                    launch(CommonPool) {
                        delay(randomOf(*randomLatency).toLong())
                        toY.send(CallComponents(time, id, call, arg))
                    }
                else
                    runBlocking {
                        toY.send(CallComponents(time, id, call, arg))
                    }
            }
        }

        y = object : Container(1) {
            override fun dispatch(time: Revision, id: List<Any?>, call: Byte, arg: Any?) {
                if (simulatePing)
                    launch(CommonPool) {
                        delay(randomOf(*randomLatency).toLong())
                        toX.send(CallComponents(time, id, call, arg))
                    }
                else
                    runBlocking {
                        toX.send(CallComponents(time, id, call, arg))
                    }
            }
        }

        allContainers = listOf(x, y)

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