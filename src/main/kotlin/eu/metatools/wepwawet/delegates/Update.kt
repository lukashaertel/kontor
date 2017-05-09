package eu.metatools.wepwawet.delegates

import eu.metatools.wepwawet.Entity
import kotlin.reflect.KProperty

/**
 * An update delegate, it handles updates to properties in the given dependency list.
 */
class Update<in R : Entity>(val depends: List<KProperty<*>>, val block: R.() -> Unit) {
    operator fun getValue(receiver: R, property: KProperty<*>): () -> Unit {
        return {
            receiver.parent.updateExecute(receiver, property, this)
        }
    }
}