package eu.metatools.wepwawet

import kotlin.reflect.KProperty

class Dynamic<in R : Entity<I>, I, T>(val config: Config<T>) {
    operator fun getValue(receiver: R, property: KProperty<*>): T {
        return receiver.parent.dynamicGet(receiver, property, config)
    }


    operator fun setValue(receiver: R, property: KProperty<*>, value: T) {
        receiver.parent.dynamicSet(receiver, property, value, config)
    }
}