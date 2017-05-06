package eu.metatools.wepwawet

import kotlin.reflect.KProperty

/**
 * Created by pazuzu on 5/5/17.
 */
abstract class Action< R : Entity<I>, I> {
    abstract fun execute(): Unit

    operator fun getValue(receiver: Entity<I>, property: KProperty<*>): () -> Unit {
        return {
            //TODO THIS IS WRONG
            receiver.parent.trackedExecute(receiver, property, this)
        }
    }
}