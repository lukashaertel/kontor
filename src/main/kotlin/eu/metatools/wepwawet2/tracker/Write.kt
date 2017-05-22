package eu.metatools.wepwawet2.tracker

import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.PropId

/**
 * Write of property [propId] on entity with identity [id].
 */
data class Write internal constructor(val id: Id, val propId: PropId)