package eu.metatools.common

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.Screen.DEFAULT_CHARACTER
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.TerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen;
import kotlin.properties.Delegates.notNull

class BoundScreen(
        val terminal: Terminal,
        val screen: Screen) : Terminal by terminal, Screen by screen {
    override fun close() {
        screen.close()
        terminal.close()
    }

    override fun getCursorPosition(): TerminalPosition {
        return screen.cursorPosition
    }

    override fun setCursorPosition(position: TerminalPosition?) {
        screen.cursorPosition = position
    }

    override fun getTerminalSize(): TerminalSize {
        return screen.terminalSize
    }

    override fun newTextGraphics(): TextGraphics {
        return screen.newTextGraphics()
    }

    override fun pollInput(): KeyStroke? {
        return screen.pollInput()
    }

    override fun readInput(): KeyStroke {
        return screen.readInput()
    }
}

inline fun <R> TerminalFactory.term(defaultCharacter: TextCharacter = DEFAULT_CHARACTER, block: BoundScreen.() -> R) {
    createTerminal().use {
        val screen = TerminalScreen(it, defaultCharacter)
        screen.startScreen()
        block(BoundScreen(it, screen))
        screen.stopScreen()
    }
}

inline fun <R> term(defaultCharacter: TextCharacter = DEFAULT_CHARACTER, block: BoundScreen.() -> R) =
        DefaultTerminalFactory().term(defaultCharacter, block)
