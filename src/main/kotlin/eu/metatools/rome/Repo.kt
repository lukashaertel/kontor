package eu.metatools.rome

import java.util.*

interface Action<in T, C>where T : Comparable<T> {
    fun exec(time: T): C

    fun undo(time: T, carry: C)
}


/**
 * @param T Time type
 */
class Repo<T> where T : Comparable<T> {
    /**
     * Store of all transactions
     */
    private val transactions = TreeMap<T, Pair<Action<T, *>, Any?>>()

    private fun undoAll(items: NavigableMap<T, Pair<Action<T, *>, Any?>>) {
        items.descendingMap().entries.iterator().apply {
            while (hasNext()) {
                // Get next item
                val i = next()

                // Carry is set from call itself, so the cast is safe
                @Suppress("unchecked_cast")
                (i.value.first as Action<T, Any?>).undo(i.key, i.value.second)

                // Reset the carry
                i.setValue(i.value.first to null)
            }
        }
    }

    private fun execAll(items: NavigableMap<T, Pair<Action<T, *>, Any?>>) {
        items.entries.iterator().apply {
            while (hasNext()) {
                // Get next item
                val i = next()

                // Execute and set the carry
                i.setValue(i.value.first to i.value.first.exec(i.key))
            }
        }
    }

    fun remove(action: Action<T, *>, time: T): Boolean {
        // Remove the item
        val x = transactions.remove(time)

        // Not a member, return false
        if (x == null)
            return false

        // Member but mismatching action, restore and return false
        if (x.first != action) {
            transactions.put(time, x)
            return false
        }

        // Undo all subsequent states, then undo this, then execute subsequent states
        val items = transactions.tailMap(time, false)
        undoAll(items)

        // Carry is set from call itself, so the cast is safe
        @Suppress("unchecked_cast")
        (x.first as Action<T, Any?>).undo(time, x.second)

        execAll(items)
        return true
    }

    fun insert(action: Action<T, *>, time: T): Boolean {
        // Slot already occupied, return false
        if (time in transactions)
            return false


        // Undo all subsequent states, then undo this, then execute subsequent states
        val items = transactions.tailMap(time, false)
        undoAll(items)

        // Carry is set from call itself, so the cast is safe
        @Suppress("unchecked_cast")
        val carry = action.exec(time)
        transactions.put(time, action to carry)

        execAll(items)
        return true
    }

    fun softUpper(until: T?) {
        TODO("Introduce soft upper bound to limit execution to time.")
    }

    fun drop(until: T) {
        transactions.headMap(until, false).clear()
    }
}