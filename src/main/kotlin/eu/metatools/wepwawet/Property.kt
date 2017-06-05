package eu.metatools.wepwawet

import eu.metatools.wepwawet.util.provideDelegate
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Entity member property.
 */
class Property<in R : Entity, T> internal constructor(
        val memberId: MemberId,
        val mutable: Boolean) {

    operator fun getValue(r: R, p: KProperty<*>): T {
        @Suppress("unchecked_cast")
        return r.node[memberId] as T
    }

    operator fun setValue(r: R, p: KProperty<*>, t: T) {
        if (!mutable)
            throw UnsupportedOperationException("Trying to mutate an immutable field")

        r.node[memberId] = t
    }
}

/**
 * Provides a [Property].
 */
fun <R : Entity, T> R.prop(initial: T) = provideDelegate { r: R, p ->
    @Suppress("unchecked_cast")
    Property<R, T>(r.node.repo.mapper.get(p as KProperty1<Any, *>), p is KMutableProperty1<*, *>).also {
        r.node[it.memberId] = initial
    }
}