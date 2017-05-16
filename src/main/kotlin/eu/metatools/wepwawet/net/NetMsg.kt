package eu.metatools.wepwawet.net

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.serialization.Serializable

/**
 * Base interface for [eu.metatools.wepwawet.Wepwawet] messages.
 */
interface NetMsg

/**
 * A request of an identifier group lease
 */
@Serializable
class IdLeaseRequest : NetMsg

/**
 * The result of an identifier group lease request
 */
@Serializable
data class IdLease(val from: Int, val to: Int) : NetMsg

/**
 * Call to an impulse with the given parameter in the given time
 */
@Serializable
data class ImpulseFired(val id: Int, val impulse: KProperty<*>, val parameter: Any?, val time: Long) : NetMsg

/**
 * Request of full data.
 */
@Serializable
class FullDataRequest : NetMsg

/**
 * Response of full data request with the number of sending entities.
 */
@Serializable
data class FullDataIncoming(val size: Int)

/**
 * An initializer of an entity.
 * @param id The ID of the entity
 * @param entityClass The class of the entity
 * @param parameters The remaining arguments passed to the class
 * @param assignments The field assignments of the entity
 */
@Serializable
data class FullDataSingle(
        val id: Int,
        val entityClass: KClass<*>,
        val parameters: List<Any?>,
        val assignments: Map<KProperty<*>, Any?>) : NetMsg

/**
 * A chunk of entity initializers.
 */
@Serializable
data class FullDataChunk(val entries: List<FullDataSingle>)

/**
 * A ping request sent from the server.
 */
@Serializable
data class Ping(val seed: Long) : NetMsg

/**
 * A ping response sent back to the server.
 */
@Serializable
data class Pong(val seed: Long) : NetMsg