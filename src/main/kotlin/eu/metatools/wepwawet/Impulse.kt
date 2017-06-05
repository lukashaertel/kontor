package eu.metatools.wepwawet

import eu.metatools.wepwawet.util.provideDelegate
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Base class for impulses that have an erased form.
 */
abstract class BaseImpulse {
    /**
     * Type erased method variant.
     */
    internal abstract val erased: Entity.(Any?) -> Unit
}

/**
 * No input impulse.
 */
class UnitImpulse<in R : Entity> internal constructor(
        val memberId: MemberId,
        val block: R.() -> Unit) : BaseImpulse() {

    @Suppress("unchecked_cast")
    override val erased: Entity.(Any?) -> Unit = { _ ->
        block(this as R)
    }

    operator fun getValue(r: R, p: KProperty<*>): () -> Unit = { ->
        r.node.repo.impulse(Call(r.node.id, memberId, Unit))
        r.node.repo.head = r.node.repo.head.incMinor()
    }
}

/**
 * Single input impulse.
 */
class Impulse<in R : Entity, in T> internal constructor(
        val memberId: MemberId,
        val block: R.(T) -> Unit) : BaseImpulse() {

    @Suppress("unchecked_cast")
    override val erased: Entity.(Any?) -> Unit = { a ->
        val t = a as T
        block(this as R, t)
    }

    operator fun getValue(r: R, p: KProperty<*>): (T) -> Unit = { t ->
        r.node.repo.impulse(Call(r.node.id, memberId, t))
        r.node.repo.head = r.node.repo.head.incMinor()
    }
}

/**
 * Pair input impulse.
 */
class BiImpulse<in R : Entity, in T, in U> internal constructor(
        val memberId: MemberId,
        val block: R.(T, U) -> Unit) : BaseImpulse() {

    @Suppress("unchecked_cast")
    override val erased: Entity.(Any?) -> Unit = { a ->
        val (t, u) = a as Pair<T, U>
        block(this as R, t, u)
    }

    operator fun getValue(r: R, p: KProperty<*>): (T, U) -> Unit = { t, u ->
        r.node.repo.impulse(Call(r.node.id, memberId, Pair(t, u)))
        r.node.repo.head = r.node.repo.head.incMinor()
    }
}

/**
 * Triple input impulse.
 */
class TriImpulse<in R : Entity, in T, in U, in V> internal constructor(
        val memberId: MemberId,
        val block: R.(T, U, V) -> Unit) : BaseImpulse() {

    @Suppress("unchecked_cast")
    override val erased: Entity.(Any?) -> Unit = { a ->
        val (t, u, v) = a as Triple<T, U, V>
        block(this as R, t, u, v)
    }

    operator fun getValue(r: R, p: KProperty<*>): (T, U, V) -> Unit = { t, u, v ->
        r.node.repo.impulse(Call(r.node.id, memberId, Triple(t, u, v)))
        r.node.repo.head = r.node.repo.head.incMinor()
    }
}

/**
 * Provides an [Impulse].
 */
@JvmName("unitImpulse")
fun <R : Entity> R.impulse(block: R.() -> Unit) = provideDelegate { r: R, p ->
    @Suppress("unchecked_cast")
    UnitImpulse(r.node.repo.mapper.get(p as KProperty1<Any, *>), block).also {
        r.node.repo.registerErasedCall(it.memberId, it.erased)
    }
}

/**
 * Provides an [Impulse].
 */
@JvmName("impulse")
fun <R : Entity, T> R.impulse(block: R.(T) -> Unit) = provideDelegate { r: R, p ->
    @Suppress("unchecked_cast")
    Impulse(r.node.repo.mapper.get(p as KProperty1<Any, *>), block).also {
        r.node.repo.registerErasedCall(it.memberId, it.erased)
    }
}

/**
 * Provides a [BiImpulse].
 */
@JvmName("biImpulse")
fun <R : Entity, T, U> R.impulse(block: R.(T, U) -> Unit) = provideDelegate { r: R, p ->
    @Suppress("unchecked_cast")
    BiImpulse(r.node.repo.mapper.get(p as KProperty1<Any, *>), block).also {
        r.node.repo.registerErasedCall(it.memberId, it.erased)
    }
}

/**
 * Provides a [TriImpulse].
 */
@JvmName("triImpulse")
fun <R : Entity, T, U, V> R.impulse(block: R.(T, U, V) -> Unit) = provideDelegate { r: R, p ->
    @Suppress("unchecked_cast")
    TriImpulse(r.node.repo.mapper.get(p as KProperty1<Any, *>), block).also {
        r.node.repo.registerErasedCall(it.memberId, it.erased)
    }
}