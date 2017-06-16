package eu.metatools.kontor

import eu.metatools.kontor.serialization.serializerOf
import kotlinx.coroutines.experimental.Job
import rice.environment.Environment
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.KSerializer

/**
 * Created by pazuzu on 6/13/17.
 */
class KontorNode(
        charset: Charset = Charsets.UTF_8,
        serializers: List<KSerializer<*>>,
        environment: Environment = DEFAULT_ENVIRONMENT) : BaseKontorNode(charset, serializers, environment) {
    override fun start(host: String, port: Int, instance: String): Job {
        return start(false, host, port, "localhost", port + 1, instance)
    }

    constructor(vararg serializers: KSerializer<*>)
            : this(serializers = listOf(*serializers))

    constructor(charset: Charset, vararg serializers: KSerializer<*>)
            : this(charset, listOf(*serializers))

    constructor(vararg kClasses: KClass<*>)
            : this(serializers = kClasses.map { serializerOf(it) })

    constructor(charset: Charset, vararg kClasses: KClass<*>)
            : this(charset, kClasses.map { serializerOf(it) })
}