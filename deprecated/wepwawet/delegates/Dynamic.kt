package eu.metatools.wepwawet.delegates

import eu.metatools.wepwawet.Entity
import kotlin.reflect.KProperty

class Dynamic<in R : Entity, T>(val config: Config<T>) {
    operator fun getValue(receiver: R, property: KProperty<*>): T {
        return receiver.parent.dynamicGet(receiver, property, this)
    }

    operator fun setValue(receiver: R, property: KProperty<*>, value: T) {
        receiver.parent.dynamicSet(receiver, property, value, this)
    }
}