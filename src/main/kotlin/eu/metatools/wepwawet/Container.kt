package eu.metatools.wepwawet

import eu.metatools.rome.Action
import eu.metatools.rome.Repo
import org.funktionale.collections.prependTo

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
    private val index = hashMapOf<List<Any>, Entity>()

    /**
     * Registers an entity with the index.
     */
    internal fun registerEntity(entity: Entity) {
        if (entity.hasKey())
            if (index.put(entity.primaryKey(), entity) != null)
                throw IllegalStateException("Entity $entity has overlapping key.")
    }

    /**
     * Unregisters an entity from the index.
     */
    internal fun unregisterEntity(entity: Entity) {
        if (entity.hasKey())
            if (!index.remove(entity.primaryKey(), entity))
                throw IllegalStateException("Entity $entity has internal mutation on key.")
    }

    /**
     * Gets the entity for the ID.
     */
    fun find(id: List<Any>) =
            index[id]

    /**
     * Finds all matching entities, where null entries are arbitrary.
     */
    fun match(id: List<Any?>) =
            index.filterKeys {
                // Same size and same positions have matching values
                id.size == it.size && (id zip it).all { (a, b) -> a == null || a == b }
            }

    /**
     * Finds all matching entities, where null entries are arbitrary, including a type key.
     */
    @JvmName("matchWithTypeKey")
    inline fun <reified T> match(id: List<Any?>): Map<List<Any>, T> =
            match(T::class.java.simpleName prependTo id).mapValues { (_, v) -> v as T }


    /**
     * Dispatches the [call] on [id] with argument [arg].
     */
    abstract fun dispatch(time: Revision, id: List<Any>, call: Byte, arg: Any?)


    /**
     * Handles an external [call] on [id] with argument [arg].
     */
    fun receive(time: Revision, id: List<Any>, call: Byte, arg: Any?) {
        // Insert into repository
        repo.insert(object : Action<Revision, ContainerUndo?> {
            override fun exec(time: Revision): ContainerUndo? {
                // Resolve entity, if not present, don't do anything
                val target = find(id) ?: return null

                println("Doing $call on $id")

                // Otherwise execute nested action and return composed undo
                val nestedAction = target.runAction(call, arg)
                val nestedUndo = nestedAction.exec(time)
                return ContainerUndo(target, nestedAction, nestedUndo)
            }

            override fun undo(time: Revision, carry: ContainerUndo?) {
                // If Exec was successful, undo
                if (carry != null) {
                    println("Undoing $call on $id")
                    @Suppress("unchecked_cast")
                    (carry.action as Action<Revision, Any?>).undo(time, carry.carry)
                }
            }

        }, time)
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