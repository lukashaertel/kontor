package eu.metatools.kontor.serialization

import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.serialization.KSerializer

/**
 * Retrieves the serializer of the given class in the type parameter.
 */
inline fun <reified T : Any> serializerOf() =
        serializerOf(T::class)

/**
 * Retrieves the serializer of the given class in the argument.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> serializerOf(kClass: KClass<T>) =
        kClass.companionObjectInstance as KSerializer<T>

/**
 * Composes a [KSerializationHandler] with the given serializers.
 */
fun kSerializationHandler(vararg serializers: KSerializer<*>) =
        KSerializationHandler(serializers = listOf(*serializers))

/**
 * Composes a [KSerializationHandler] with the given serializers and charset.
 */
fun kSerializationHandler(charset: Charset, vararg serializers: KSerializer<*>) =
        KSerializationHandler(charset, listOf(*serializers))

/**
 * Composes a [KSerializationHandler] with the serializers of the given classes.
 */
fun kSerializationHandler(vararg kClasses: KClass<*>) =
        KSerializationHandler(serializers = kClasses.map { serializerOf(it) })

/**
 * Composes a [KSerializationHandler] with the serializers of the given classes and a charset.
 */
fun kSerializationHandler(charset: Charset, vararg kClasses: KClass<*>) =
        KSerializationHandler(charset, kClasses.map { serializerOf(it) })
