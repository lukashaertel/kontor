package eu.metatools.rome

import com.google.common.collect.Maps
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
     * Backing for the soft upper bound.
     */
    private var softUpperBacking: T? = null

    private var executingBacking = false

    val executing get() = executingBacking

    /**
     * Soft upper bound, setting this will undo or exec values after or up to the new soft upper bound. Null if no
     * soft upper bound is requested.
     */
    var softUpper: T?
        get() = softUpperBacking
        set(value) {
            // Handle shift of soft upper bound
            softUpperBacking.let {
                if (it == null) {
                    if (value == null)
                        return
                    else
                        undoAll(transactions.tailMap(value, false))
                } else {
                    if (value == null)
                        execAll(transactions.tailMap(it, false))
                    else if (it == value)
                        return
                    else if (it < value)
                        execAll(transactions.subMap(it, false, value, true))
                    else if (it > value)
                        undoAll(transactions.subMap(value, false, it, true))
                }
            }

            // Transfer value
            softUpperBacking = value
        }

    /**
     * Store of all transactions
     */
    private val transactions = TreeMap<T, Pair<Action<T, *>, Any?>>()

    /**
     * Gets all revisions that are currently in the repository.
     */
    val revisions get() = transactions.navigableKeySet().toSortedSet()

    val allActions get() = transactions.mapValues { (_, t) -> t.first }

    val pendingActions: NavigableMap<T, Action<T, *>> get() = Maps.transformValues(Maps.filterValues(transactions) {
        t ->
        t?.second == null
    }) { t ->
        t?.first
    }

    val doneActions: NavigableMap<T, Action<T, *>> get() = Maps.transformValues(Maps.filterValues(transactions) {
        t ->
        t?.second != null
    }) { t ->
        t?.first
    }


    /**
     * Undo all transactions in [items].
     */
    private fun undoAll(items: NavigableMap<T, Pair<Action<T, *>, Any?>>) {
        executingBacking = true
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
        executingBacking = false
    }

    /**
     * Exec all transactions in [items].
     */
    private fun execAll(items: NavigableMap<T, Pair<Action<T, *>, Any?>>) {
        executingBacking = true
        items.entries.iterator().apply {
            while (hasNext()) {
                // Get next item
                val i = next()

                // Execute and set the carry
                i.setValue(i.value.first to i.value.first.exec(i.key))
            }
        }
        executingBacking = false
    }

    /**
     * Removes the action, undoing and executing all subsequent actions.
     */
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

        // Preemptive cutoff if removing after the soft upper bound
        softUpperBacking.let {
            if (it != null && it < time)
                return true
        }

        // Calculate items to undo and exec
        val items = softUpperBacking.let {
            if (it == null)
                transactions.tailMap(time, false)
            else
                transactions.subMap(time, false, it, true)
        }

        // Undo all subsequent states, then undo this, then execute subsequent states
        undoAll(items)

        // Carry is set from call itself, so the cast is safe
        executingBacking = true
        @Suppress("unchecked_cast")
        (x.first as Action<T, Any?>).undo(time, x.second)
        executingBacking = false

        execAll(items)
        return true
    }

    /**
     * Adds the action, undoing and executing all subsequent actions.
     */
    fun insert(action: Action<T, *>, time: T): Boolean {
        // Slot already occupied, return false
        if (time in transactions)
            return false

        // Preemptive cutoff if inserting after the soft upper bound
        softUpperBacking.let {
            transactions.put(time, action to null)
            if (it != null && it < time)
                return true
        }

        // Calculate items to undo and exec
        val items = softUpperBacking.let {
            if (it == null)
                transactions.tailMap(time, false)
            else
                transactions.subMap(time, false, it, true)
        }

        // Undo all subsequent states, then exec this, then execute subsequent states
        undoAll(items)

        // Carry is set from call itself, so the cast is safe
        executingBacking = true
        @Suppress("unchecked_cast")
        val carry = action.exec(time)
        transactions.put(time, action to carry)
        executingBacking = false

        execAll(items)
        return true
    }

    /**
     * Removes all actions until but not including the given time.
     */
    fun drop(until: T) {
        transactions.headMap(until, false).clear()
    }
}