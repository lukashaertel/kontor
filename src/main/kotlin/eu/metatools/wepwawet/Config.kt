package eu.metatools.wepwawet

import org.funktionale.option.Option
import org.funktionale.option.Option.None
import org.funktionale.option.Option.Some

data class Config<out T>(
        val default: T)

class ConfigCollector<T> {
    private var internalDefault: Option<T> = None

    var default: T
        get() = internalDefault.get()
        set(value) {
            internalDefault = Some(value)
        }

    fun finalize() = Config(
            internalDefault.get())

}

typealias Configurator<T> = ConfigCollector<T>.() -> Unit

inline fun <T> runConfigurator(configurator: Configurator<T>) =
        ConfigCollector<T>()
                .apply(configurator)
                .finalize()