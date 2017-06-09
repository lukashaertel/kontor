package eu.metatools.common

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.TerminalFactory

/**
 * Runs a terminal.
 */
fun <R> terminal(block: Terminal.() -> R) =
        DefaultTerminalFactory().terminal(block)

/**
 * Runs a terminal with a suspend block.
 */
suspend fun <R> sTerminal(block: suspend Terminal.() -> R) =
        DefaultTerminalFactory().sTerminal(block)

/**
 * Runs a terminal with the given terminal factory.
 */
fun <R> TerminalFactory.terminal(block: Terminal.() -> R) =
        createTerminal().use(block)

/**
 * Runs a terminal with the given terminal factory with a suspend block.
 */
suspend fun <R> TerminalFactory.sTerminal(block: suspend Terminal.() -> R) {
    val t = createTerminal()
    try {
        block(t)
    } finally {
        t.flush()
        t.close()
    }
}

/**
 * Runs a block with the text graphics.
 */
fun <R> Terminal.text(block: TextGraphics.() -> R) =
        newTextGraphics().block()

/**
 * Runs a block with the text graphics with a suspend block.
 */
suspend fun <R> Terminal.sText(block: suspend TextGraphics.() -> R) =
        newTextGraphics().block()
