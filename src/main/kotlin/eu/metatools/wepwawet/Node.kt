package eu.metatools.wepwawet

import java.util.*

/**
 * Underlying storage node, operations are relative to [repo]'s [Repo.head] value. Relevant members are internal,
 * [Node] will be created by [Repo.construct] (or [Entity.construct]).
 *
 * @param repo The repo the node is stored in.
 * @param id The identity of the node.
 * @param constructorId The constructor used to create the owning entity.
 * @param arguments The arguments passed to the entity.
 */
class Node(
        val repo: Repo,
        val id: Id,
        val from: Rev,
        val constructorId: ConstructorId,
        val arguments: List<Any?>) {
    /**
     * Storage array of revision-to-value mappings.
     */
    private val store = Array(repo.mapper.membersSize(constructorId.classNum)) {
        TreeMap<Rev, Any?>()
    }

    /**
     * The revision where the containing entity was deleted.
     */
    internal var to: Rev? = null

    /**
     * True if the containing repository's head revision is in the lifetime of the entity.
     */
    internal fun isAlive() = to.let { from <= repo.head && (it == null || repo.head < it) }

    /**
     * Gets the value of [memberId] at the repository's head revision.
     */
    internal operator fun get(memberId: MemberId): Any? {
        // Track access
        repo.trackRead(id, memberId)

        // Get first lower or equal value from head
        return store[memberId.memberNum.toInt()].floorEntry(repo.head).value
    }

    /**
     * Sets the value of [memberId] at the repository's head revision.
     */
    internal operator fun set(memberId: MemberId, any: Any?) {
        // Track access
        repo.trackWrite(id, memberId)

        // Set head value
        store[memberId.memberNum.toInt()][repo.head] = any
    }

    /**
     * Resets a write.
     */
    internal fun reset(memberId: MemberId) {
        // Reset access at head
        store[memberId.memberNum.toInt()].remove(repo.head)
    }

    /**
     * Signs off the values up to the repository's head revision.
     */
    internal fun signOff() {
        // Get current head
        val head = repo.head

        // Sign off on each individual row
        for (row in store) {
            // If there is no mapping at head, use next lower key
            if (head !in row)
                row.lowerKey(head).let {
                    if (it != null)
                        row.headMap(it, false).clear()
                }
            else
            // Clear all lower values
                row.headMap(head, false).clear()
        }
    }

    override fun toString() = buildString {
        append("Node(id=")
        append(id)
        appendln("):")
        for ((i, row) in store.withIndex()) {
            // Build member ID
            val memberId = MemberId(constructorId.classNum, i.toByte())

            // Get name and entry
            val name = repo.mapper.name(memberId)
            val entry = row.floorEntry(repo.head)

            // Append row
            append("  ")
            append(name)
            if (entry == null)
                appendln(" has backing error")
            else {
                append(" = ")
                append(entry.value)
                append('@')
                appendln(entry.key)
            }
        }
    }
}