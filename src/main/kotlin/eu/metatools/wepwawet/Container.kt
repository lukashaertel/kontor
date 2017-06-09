package eu.metatools.wepwawet

import eu.metatools.rome.Action
import eu.metatools.rome.Repo
import java.util.*

/**
 * Undo in at container level, includes entity lookup.
 */
private data class ContainerUndo(
        val entity: Entity,
        val action: Action<Revision, *>,
        val carry: Any?)

/**
 * Exchange manager and change tracker.
 */
abstract class Container(val author: Byte) {
    /**
     * Current insert time.
     */
    var time: Int = 0

    /**
     * Computes a new [Revision] object representing the current time and authorship.
     */
    fun rev() =
            Revision(time, author)

    /**
     * Repository for change rollback.
     */
    internal val repo = Repo<Revision>()

    /**
     * Map of primary key to entities.
     */
    private val indexBacking = hashMapOf<List<Any>, Entity>()

    /**
     * Gets the index of the container.
     */
    val index: Map<List<Any>, Entity> = Collections.unmodifiableMap(indexBacking)

    /**
     * Registers an entity with the index.
     */
    internal fun registerEntity(entity: Entity) {
        if (entity.hasKey())
            if (indexBacking.put(entity.primaryKey(), entity) != null)
                throw IllegalStateException("Entity $entity has overlapping key.")
    }

    /**
     * Unregisters an entity from the index.
     */
    internal fun unregisterEntity(entity: Entity) {
        if (entity.hasKey())
            if (!indexBacking.remove(entity.primaryKey(), entity))
                throw IllegalStateException("Entity $entity has internal mutation on key.")
    }

    /**
     * Gets the entity for the ID.
     */
    fun find(id: List<Any>) =
            indexBacking[id]

    /**
     * Finds all matching entities, where null entries are arbitrary.
     */
    fun match(id: List<Any?>) =
            indexBacking.filterKeys {
                // Same size and same positions have matching values
                id.size == it.size && (id zip it).all { (a, b) -> a == null || a == b }
            }

    /**
     * Finds all matching entities of type [T], where null entries are arbitrary. The [AutoKeyMode] will be
     * automatically filled.
     */
    @JvmName("matchWithType")
    inline fun <reified T : Entity> match(id: List<Any?>): Map<List<Any>, T> {
        val result = hashMapOf<List<Any>, T>()
        for ((k, v) in index)
            if (v is T)
                result.put(k, v)

        // No items, return immediately
        if (result.isEmpty())
            return result

        // Get first value and select by it's auto key mode.
        val first = result.values.first()
        val fullId = when (first.autoKeyMode) {
            AutoKeyMode.NONE -> id
            AutoKeyMode.ONLY_ONE -> listOf(T::class.java.simpleName) + id
            AutoKeyMode.ONLY_ONE_PER_TIME -> listOf(T::class.java.simpleName, null) + id
        }

        // Remove mismatching entries
        result.entries.iterator().apply {
            while (hasNext()) {
                val (itemId, _) = next()

                // Same size and same positions have matching values
                if (fullId.size != itemId.size)
                    remove()
                else if ((fullId zip itemId).any { (a, b) -> a != null && a != b })
                    remove()
            }
        }

        // Return the assignments
        return result
    }

    /**
     * Finds all matching entities of type [T] on revision [revision], where null entries are arbitrary. The
     * [AutoKeyMode] will be automatically filled.
     */
    @JvmName("matchWithTypeAndRevision")
    inline fun <reified T : Entity> match(revision: Revision, id: List<Any?>): Map<List<Any>, T> {
        val result = hashMapOf<List<Any>, T>()
        for ((k, v) in index)
            if (v is T)
                result.put(k, v)

        // No items, return immediately
        if (result.isEmpty())
            return result

        // Get first value and select by it's auto key mode.
        val first = result.values.first()
        val fullId = when (first.autoKeyMode) {
            AutoKeyMode.NONE -> id
            AutoKeyMode.ONLY_ONE -> listOf(T::class.java.simpleName) + id
            AutoKeyMode.ONLY_ONE_PER_TIME -> listOf(T::class.java.simpleName, revision) + id
        }

        // Remove mismatching entries
        result.entries.iterator().apply {
            while (hasNext()) {
                val (itemId, _) = next()

                // Same size and same positions have matching values
                if (fullId.size != itemId.size)
                    remove()
                else if ((fullId zip itemId).any { (a, b) -> a != null && a != b })
                    remove()
            }
        }

        // Return the assignments
        return result
    }

    /**
     * Dispatches the [call] on [id] with argument [arg].
     */
    abstract fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?)


    /**
     * Handles an external [call] on [id] with argument [arg].
     */
    fun receive(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
        synchronized(repo) {
            // Insert into repository
            repo.insert(object : Action<Revision, ContainerUndo?> {
                override fun exec(time: Revision): ContainerUndo? {
                    // Resolve entity, if not present, don't do anything
                    val target = find(id) ?: return null

                    // Otherwise execute nested action and return composed undo
                    val nestedAction = target.runAction(call, arg)
                    val nestedUndo = nestedAction.exec(time)
                    return ContainerUndo(target, nestedAction, nestedUndo)
                }

                override fun undo(time: Revision, carry: ContainerUndo?) {
                    // If Exec was successful, undo
                    if (carry != null) {
                        @Suppress("unchecked_cast")
                        (carry.action as Action<Revision, Any?>).undo(time, carry.carry)
                    }
                }

            }, time)
        }
    }

    /**
     * Initializes the backing repository with the container, this is then the root for exchanging calls.
     */
    fun <E : Entity> init(constructor: (Container) -> E): E {
        return constructor(this).also {
            registerEntity(it)
        }
    }
}