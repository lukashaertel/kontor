package eu.metatools.wepwawet.delegates

import eu.metatools.wepwawet.Entity
import eu.metatools.wepwawet.tools.provideDelegate
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Provides a static value delegate, automatically registers unregistered items in the provided [Wepwawet] instance.
 * It is suggested to use [staticOf], since it lifts type inference problems and fills out the mandatory default
 * value.
 */
@Suppress("UNUSED")
inline fun <R : Entity, T> R.static(crossinline conf: Configurator<T>) =
        provideDelegate { r: R, p: KProperty<*> ->
            Static<R, T>(runConfigurator(conf)).apply {
                r.parent.registerStatic(p, this)
                r.parent.staticInit(r, p, this)
            }
        }

fun <R : Entity, T> R.staticOf(default: T) =
        static<R, T> {
            this.default = default
        }

inline fun <R : Entity, T> R.staticOf(default: T, crossinline conf: Configurator<T>) =
        static<R, T> {
            conf()
            this.default = default
        }

/**
 * Provides a dynamic value delegate, automatically registers unregistered items in the provided [Wepwawet] instance.
 * It is suggested to use [dynamicOf], since it lifts type inference problems and fills out the mandatory default
 * value.
 */
@Suppress("UNUSED")
inline fun <R : Entity, T> R.dynamic(crossinline conf: Configurator<T>) =
        provideDelegate { r: R, p: KProperty<*> ->
            if (p !is KProperty1<*, *>)
                throw IllegalArgumentException("Only supported for member properties")
            Dynamic<R, T>(runConfigurator(conf)).apply {
                r.parent.registerDynamic(p, this)
                r.parent.dynamicInit(r, p, this)
            }
        }


fun <R : Entity, T> R.dynamicOf(default: T) =
        dynamic<R, T> {
            this.default = default
        }

inline fun <R : Entity, T> R.dynamicOf(default: T, crossinline conf: Configurator<T>) =
        dynamic<R, T> {
            conf()
            this.default = default
        }

fun <R : Entity> R.reactTo(vararg kProperty: KProperty<*>, execute: R.() -> Unit) =
        provideDelegate { r: R, p: KProperty<*> ->
            Update(kProperty.toSet(), execute).apply {
                r.parent.registerUpdate(p, this)
            }
        }

// TODO Consistency checks to revert only partially
fun <R : Entity, T> R.impulse(execute: R.(T) -> Unit) =
        provideDelegate { r: R, p: KProperty<*> ->
            Impulse(execute).apply {
                r.parent.registerImpulse(p, this)
            }
        }
