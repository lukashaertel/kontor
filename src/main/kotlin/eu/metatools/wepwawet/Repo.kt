package eu.metatools.wepwawet

import java.util.*
import kotlin.reflect.KFunction


open class Repo(val mapper: Mapper) {
    /**
     * Table of all tracked entities
     */
    private val entities = TreeMap<Id, Entity>()

    /**
     * Table of member ID to type-erased method definitions.
     */
    private val erasedCalls = hashMapOf<MemberId, Entity.(Any?) -> Unit>()


    /**
     * Executed calls that might need to be re-weaved
     */
    private val executedCalls = TreeMap<Rev, Pair<Call, Deps>>()

    /**
     * Current revision
     */
    var head: Rev = Rev(0, 0, 0)

    /**
     * Tracking field for entities that are operated on.
     */
    private val trackOperates = arrayListOf<Id>()

    /**
     * Tracking field for read access.
     */
    private val trackReads = arrayListOf<Pair<Id, MemberId>>()

    /**
     * Tracking field for write access.
     */
    private val trackWrites = arrayListOf<Pair<Id, MemberId>>()

    /**
     * Tracking field for construction.
     */
    private val trackConstructs = arrayListOf<Id>()

    /**
     * Tracking field for destruction.
     */
    private val trackDeletes = arrayListOf<Id>()

    /**
     * Resets the tracking fields.
     */
    private fun reset() {
        trackOperates.clear()
        trackReads.clear()
        trackWrites.clear()
        trackConstructs.clear()
        trackDeletes.clear()
    }

    /**
     * Store of impulse dispatches.
     */
    private var rootImpulse: Call? = null

    /**
     * Gets all current entities, read only copy.
     */
    fun headEntities() =
            entities.filterValues { it.node.isAlive() }


    internal fun registerErasedCall(memberId: MemberId, erased: Entity.(Any?) -> Unit) {
        erasedCalls[memberId] = erased
    }

    /**
     * Tracks a read access from a [Property].
     */
    internal fun trackRead(id: Id, memberId: MemberId) {
        trackOperates += id
        trackReads += id to memberId
    }

    /**
     * Tracks a write access from a [Property].
     */
    internal fun trackWrite(id: Id, memberId: MemberId) {
        trackOperates += id
        trackWrites += id to memberId
    }

    /**
     * Handles construction of an entity.
     */
    private fun <R : Entity> runConstruct(erased: (Node) -> R, constructorId: ConstructorId, args: List<Any?>): R {
        // Create result
        return erased(Node(this, head, head, constructorId, args)).also {
            // Add to entity table
            entities[head] = it

            // Track construction
            trackConstructs += head

            // Increase minor counter
            head = head.incMinor()

            // Run post constructor hook
            it.constructed()
        }
    }

    /**
     * Constructs an entity with the given constructor (may not be lambda).
     */
    fun <R : Entity> construct(constructor: (Node) -> R) =
            @Suppress("unchecked_cast")
            runConstruct({ constructor(it) }, mapper.get(constructor as KFunction<Any>), listOf())

    /**
     * Constructs an entity with the given constructor (may not be lambda).
     */
    fun <R : Entity, T1> construct(constructor: (Node, T1) -> R, t1: T1) =
            @Suppress("unchecked_cast")
            runConstruct({ constructor(it, t1) }, mapper.get(constructor as KFunction<Any>), listOf(t1))

    /**
     * Constructs an entity with the given constructor (may not be lambda).
     */
    fun <R : Entity, T1, T2> construct(constructor: (Node, T1, T2) -> R, t1: T1, t2: T2) =
            @Suppress("unchecked_cast")
            runConstruct({ constructor(it, t1, t2) }, mapper.get(constructor as KFunction<Any>), listOf(t1, t2))

    /**
     * Constructs an entity with the given constructor (may not be lambda).
     */
    fun <R : Entity, T1, T2, T3> construct(constructor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3) =
            @Suppress("unchecked_cast")
            runConstruct({ constructor(it, t1, t2, t3) }, mapper.get(constructor as KFunction<Any>), listOf(t1, t2, t3))

    /**
     * Deletes the entity.
     */
    fun delete(entity: Entity) {
        // Remove entity virtually and call destructor
        entity.deleting()
        entity.node.to = head

        // Track operate and destruction
        trackOperates += entity.node.id
        trackDeletes += entity.node.id
    }

    /**
     * Interception for local execution
     */
    protected open fun onRootImpulse(rev: Rev, call: Call) {
        // Do nothing
    }


    /**
     * Specialized variant of insert, where a listener is notified.
     */
    internal fun impulse(call: Call) {
        if (rootImpulse == null)
            rootImpulse = call

        if (rootImpulse == call)
            onRootImpulse(head, call)

        insert(call)

        if (rootImpulse == call)
            rootImpulse = null
    }

    /**
     * Inserts the call at the [head] revision, executing it and all invalidated calls.
     */
    fun insert(call: Call) {
        insert(head, call)
    }

    /**
     * Inserts the call at the given time, executing it and all invalidated calls.
     */
    fun insert(rev: Rev, call: Call) {
        // Store previous time
        val prev = head

        // Shared set of invalidated calls
        val invalidated = TreeMap<Rev, Pair<Call, Deps>>()

        // Recursive method to run a call in a revision, enriching the invalidated set
        tailrec fun doInsert(rev: Rev, call: Call) {
            // Set current weave revision
            head = rev

            // Try to find entity
            val entity = entities[call.id]

            // Check if exists and alive
            if (entity != null && entity.node.isAlive()) {
                // Reset tracking variables
                reset()

                // Add main operand
                trackOperates += call.id

                // Call method
                erasedCalls.getValue(call.memberId).invoke(entity, call.arg)

                // Build dependency map
                val deps = Deps(
                        trackOperates.toList(),
                        trackReads.toList(),
                        trackWrites.toList(),
                        trackConstructs.toList(),
                        trackDeletes.toList())

                // Add all directly invalidated executed calls
                for ((r, e) in executedCalls.tailMap(rev, false))
                    if (deps invalidates e.second)
                        invalidated[r] = e

                // Insert own call and dependencies
                executedCalls[rev] = call to deps
            }

            // If there are remaining invalidated entries
            if (invalidated.isNotEmpty()) {
                // Remove and decompose first entry
                val (r, e) = invalidated.pollFirstEntry()
                val (c, d) = e

                // Transfer time of invalidated call
                head = r

                // Remove all not constructed entities
                for (i in d.constructs)
                    entities.remove(i)

                // Restore all not deleted entities
                for (i in d.deletes)
                    entities[i]?.node?.to = null

                // Reset all non written accesses
                for ((i, m) in d.writes)
                    entities[i]?.node?.reset(m)

                doInsert(r, c)
            }
        }

        // Execute the recursive algorithm
        doInsert(rev, call)

        // Restore previous time
        head = prev
    }

    /**
     * Inserts the calls at the given time, executing them and all invalidated calls.
     */
    fun insert(calls: SortedMap<Rev, Call>) {
        for ((rev, call) in calls)
            insert(rev, call)
    }

    /**
     * Signs off on all values and entities before the [head] revision.
     */
    fun signOff() {
        // Get current head
        val head = head

        // Sign off on each individual entity
        entities.values.iterator().apply {
            while (hasNext()) {
                val e = next()
                if (e.node.to.let { it != null && it <= head })
                // If entity lifetime is over, remove it
                    remove()
                else
                // Otherwise sign off entity properties
                    e.node.signOff()
            }
        }
    }
}