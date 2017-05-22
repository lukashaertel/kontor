package eu.metatools.wepwawet2.tracker

import eu.metatools.wepwawet2.Args
import eu.metatools.wepwawet2.CtorId
import eu.metatools.wepwawet2.Id

/**
 * Tracks construction of classes via constructor [ctorId], with arguments [args] and assigned the
 * given identity [id].
 */
data class Ctor internal constructor(val id: Id, val ctorId: CtorId, val args: Args) {
    override fun hashCode() =
            13 * (13 * (13 + id.hashCode()) + ctorId.hashCode() + args.contentHashCode())

    override fun equals(other: Any?) =
            other is Ctor
                    && id == other.id
                    && ctorId == other.ctorId
                    && args.contentEquals(other.args)
}