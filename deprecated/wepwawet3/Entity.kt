package eu.metatools.wepwawet

/**
 * Baseclass for tracked entities that support [Property]s and [Impulse]s.
 */
interface Entity {
    /**
     * The storage and synchronization node, to be implemented as a constructor parameter.
     */
    val node: Node

    /**
     * Called after the entity is constructed.
     */
    fun constructed() {}

    /**
     * Called before the entity is deleted.
     */
    fun deleting() {}
}


/**
 * Constructs an entity from a constructor.
 */
fun <R : Entity> Entity.construct(constructor: (Node) -> R) =
        node.repo.construct(constructor)

/**
 * Constructs an entity from a constructor with the given argument.
 */
fun <R : Entity, T1> Entity.construct(constructor: (Node, T1) -> R, t1: T1) =
        node.repo.construct(constructor, t1)

/**
 * Constructs an entity from a constructor with the given arguments.
 */
fun <R : Entity, T1, T2> Entity.construct(constructor: (Node, T1, T2) -> R, t1: T1, t2: T2) =
        node.repo.construct(constructor, t1, t2)

/**
 * Constructs an entity from a constructor with the given arguments.
 */
fun <R : Entity, T1, T2, T3> Entity.construct(constructor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3) =
        node.repo.construct(constructor, t1, t2, t3)

/**
 * Deletes the entity.
 */
fun Entity.delete(entity: Entity) =
        node.repo.delete(entity)

/**
 * Deletes the receiver.
 */
fun Entity.delete() =
        node.repo.delete(this)