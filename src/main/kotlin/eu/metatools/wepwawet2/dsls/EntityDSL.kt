package eu.metatools.wepwawet2.dsls

import eu.metatools.wepwawet2.*
import eu.metatools.wepwawet2.tools.cast
import eu.metatools.wepwawet2.tools.provideDelegate
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

private fun legal(p: KProperty<*>): Boolean {
    // Decide if legal and if mutable
    return when (p) {
        is KMutableProperty1<*, *> -> true
        is KProperty1<*, *> -> false
        else -> error("Only supported for member properties")
    }
}


/**
 * Creates a property that reads and writes revisioned values.
 */
fun <R : Entity, T> R.prop(initial: T) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Assign initial and return delegate
    Prop.doSet(r, propId, initial)
    Prop<R, T>(propId, legal(p))
}


fun <R : Entity, T : Entity> R.ref(initial: T) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Assign initial and return delegate
    EntityProp.doSet(r, propId, initial)
    EntityProp<R, T>(propId, legal(p))
}

fun <R : Entity, T : Entity> R.refs(initial: List<T>) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Assign initial and return delegate
    EntityListProp.doSet(r, propId, initial)
    EntityListProp<R, T>(propId, legal(p))
}

fun <R : Entity> R.react(vararg to: KProperty1<R, *>, block: R.() -> Unit) = provideDelegate { r: R, p ->
    error("Unsupported")
    val depends = to.map { r.node.container.mapper.mapProp(cast(it)) }

    r.node.container.registerReact(depends, { block(cast(this)) })

    React(depends, block)
}

/**
 * Creates an impulse.
 */
@JvmName("impulse0")
fun <R : Entity> R.impulse(block: R.() -> Unit) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Register for external exchange
    r.node.container.registerImpulse(propId, { block(cast(this)) })

    // Create impulse on impulse identity and given block
    Impulse0(propId, block)
}

/**
 * Creates an impulse.
 */
@JvmName("impulse1")
fun <R : Entity, T1> R.impulse(block: R.(T1) -> Unit) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Register for external exchange
    r.node.container.registerImpulse(propId, { block(cast(this), cast(it[0])) })

    // Create impulse on impulse identity and given block
    Impulse1(propId, block)
}

/**
 * Creates an impulse.
 */
@JvmName("impulse2")
fun <R : Entity, T1, T2> R.impulse(block: R.(T1, T2) -> Unit) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Register for external exchange
    r.node.container.registerImpulse(propId, { block(cast(this), cast(it[0]), cast(it[1])) })

    // Create impulse on impulse identity and given block
    Impulse2(propId, block)
}

/**
 * Creates an impulse.
 */
@JvmName("impulse3")
fun <R : Entity, T1, T2, T3> R.impulse(block: R.(T1, T2, T3) -> Unit) = provideDelegate { r: R, p ->
    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Register for external exchange
    r.node.container.registerImpulse(propId, { block(cast(this), cast(it[0]), cast(it[1]), cast(it[2])) })

    // Create impulse on impulse identity and given block
    Impulse3(propId, block)
}

/**
 * Gets the identity of the container's network.
 */
val Entity.netIdentity get() = node.container.net.identity

/**
 * Gets a net safe random double.
 */
fun Entity.random() =
        node.container.net.random()

/**
 * Gets a net safe random integer.
 */
fun Entity.random(range: IntRange) =
        node.container.net.random(range)

/**
 * Gets a net safe random long.
 */
fun Entity.random(range: LongRange) =
        node.container.net.random(range)

/**
 * Creates the entity in tracked mode via the given constructor.
 */
fun <R : Entity> Entity.create(ctor: (Node) -> R) =
        node.container.create(ctor)

/**
 * Creates the entity in tracked mode via the given constructor.
 */
fun <R : Entity, T1> Entity.create(ctor: (Node, T1) -> R, t1: T1) =
        node.container.create(ctor, t1)

/**
 * Creates the entity in tracked mode via the given constructor.
 */
fun <R : Entity, T1, T2> Entity.create(ctor: (Node, T1, T2) -> R, t1: T1, t2: T2) =
        node.container.create(ctor, t1, t2)

/**
 * Creates the entity in tracked mode via the given constructor.
 */
fun <R : Entity, T1, T2, T3> Entity.create(ctor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3) =
        node.container.create(ctor, t1, t2, t3)

fun Entity.evict(entity: Entity) =
        node.container.evict(entity)

fun Entity.evict() =
        node.container.evict(this)