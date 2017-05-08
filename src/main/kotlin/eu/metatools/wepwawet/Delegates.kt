package eu.metatools.wepwawet

import kotlin.reflect.KProperty

/**
 * Abstract interface for providers so that they can be implemented by an anonymous object.
 */
interface Provider<in R, out T> {
    operator fun provideDelegate(receiver: R, property: KProperty<*>): T
}

/**
 * Inline variant of providing a delegate.
 * @param R The receiver type
 * @param T The delegate type
 * @param block The block to run for the delegate
 */
inline fun <R, T> provideDelegate(crossinline block: (R, KProperty<*>) -> T) = object : Provider<R, T> {
    override fun provideDelegate(receiver: R, property: KProperty<*>): T {
        return block(receiver, property)
    }
}

/**
 * Provides a static value delegate, automatically registers unregistered items in the provided [Wepwawet] instance.
 * It is suggested to use [staticOf], since it lifts type inference problems and fills out the mandatory default
 * value.
 */
@Suppress("UNUSED")
inline fun <R : Entity<I>, I, T> R.static(crossinline conf: Configurator<T>) =
        provideDelegate { r: R, p: KProperty<*> ->
            // Get config
            val config = runConfigurator(conf)

            // Register in wepwawet
            if (r !in r.parent)
                r.parent.register(r)

            // Transfer default value
            r.parent.staticInit(r, p, config.default, config)

            // Return delegate
            Static<R, I, T>(config)
        }

fun <R : Entity<I>, I, T> R.staticOf(default: T) =
        static<R, I, T> {
            this.default = default
        }

inline fun <R : Entity<I>, I, T> R.staticOf(default: T, crossinline conf: Configurator<T>) =
        static<R, I, T> {
            conf()
            this.default = default
        }

/**
 * Provides a dynamic value delegate, automatically registers unregistered items in the provided [Wepwawet] instance.
 * It is suggested to use [dynamicOf], since it lifts type inference problems and fills out the mandatory default
 * value.
 */
@Suppress("UNUSED")
inline fun <R : Entity<I>, I, T> R.dynamic(crossinline conf: Configurator<T>) =
        provideDelegate { r: R, p: KProperty<*> ->
            // Get config
            val config = runConfigurator(conf)

            // Register in wepwawet
            if (r !in r.parent)
                r.parent.register(r)

            // Transfer default value
            r.parent.dynamicInit(r, p, config.default, config)

            // Return delegate
            Dynamic<R, I, T>(config)
        }


fun <R : Entity<I>, I, T> R.dynamicOf(default: T) =
        dynamic<R, I, T> {
            this.default = default
        }

inline fun <R : Entity<I>, I, T> R.dynamicOf(default: T, crossinline conf: Configurator<T>) =
        dynamic<R, I, T> {
            conf()
            this.default = default
        }

// TODO Consistency checks to revert only partially
fun <R : Entity<I>, I, T> R.impulse(execute: R.(T) -> Unit) =
        provideDelegate { r: R, p: KProperty<*> ->
            r.parent.registerImpulse(p, execute)
            Impulse(execute)
        }

