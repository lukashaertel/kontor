package eu.metatools.wepwawet2.dsls

import eu.metatools.wepwawet2.*
import eu.metatools.wepwawet2.tools.cast
import eu.metatools.wepwawet2.tools.provideDelegate
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * Creates a property that reads and writes revisioned values.
 */
fun <R : Entity, T> R.prop(initial: T) = provideDelegate { r: R, p ->
    // Decide if legal and if mutable
    val mutable = when (p) {
        is KMutableProperty1<*, *> -> true
        is KProperty1<*, *> -> false
        else -> error("Only supported for member properties")
    }

    // Get property identity from provided property
    val propId = r.node.container.mapper.mapProp(cast(p))

    // Set in raw entity
    r.node.setValue(propId, initial)

    // Create property on the property identity with inferred mutability status
    Prop<R, T>(propId, mutable)
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
