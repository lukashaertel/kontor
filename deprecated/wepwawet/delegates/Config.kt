package eu.metatools.wepwawet.delegates

import org.funktionale.option.Option
import org.funktionale.option.Option.None
import org.funktionale.option.Option.Some

/**
 * The configuration for [Static] and [Dynamic].
 */
data class Config<out T>(
        val default: T)

/**
 * Receiver for a configuration block.
 */
class ConfigCollector<T> {
    /**
     * Internal default value, initially not set.
     */
    private var internalDefault: Option<T> = None

    /**
     * Gets or sets the current default value.
     */
    var default: T
        get() = internalDefault.get()
        set(value) {
            internalDefault = Some(value)
        }

    /**
     * Finalizes cofiguration and creates the [Config].
     */
    fun finalize() = Config(
            internalDefault.get())

}

/**
 * The type of a configurator block.
 */
typealias Configurator<T> = ConfigCollector<T>.() -> Unit

/**
 * Runs the configurator block for the resulting configuration.
 */
inline fun <T> runConfigurator(configurator: Configurator<T>) =
        ConfigCollector<T>()
                .apply(configurator)
                .finalize()