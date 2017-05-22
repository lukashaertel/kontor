package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.PropId
import eu.metatools.wepwawet2.Rev
import eu.metatools.wepwawet2.tracker.Ctor
import eu.metatools.wepwawet2.tracker.Dtor
import eu.metatools.wepwawet2.tracker.Read
import eu.metatools.wepwawet2.tracker.Write

/**
 * An impulse dependency net.
 * @param rev The revision this impulse occured on.
 * @param id The identity of the entity the impulse is executed on.
 * @param propId The identity of the executed impulse.
 * @param args The arguments passed to the impulse.
 * @param reads The [Read]s executed by the impulse.
 * @param writes The [Write]s executed by the impulse.
 * @param ctors The [Ctor]s (constructors) executed by the impulse.
 * @param ctors The [Dtor]s (constructors) executed by the impulse.
 */
data class CallNet(
        val rev: Rev,
        val id: Id,
        val propId: PropId,
        val args: List<Any?>,
        val reads: List<Read>,
        val writes: List<Write>,
        val ctors: List<Ctor>,
        val dtors: List<Dtor>) {
    val inSet by lazy { reads.map { it.id to it.propId } }

    val outSet by lazy { writes.map { it.id to it.propId } }
}

