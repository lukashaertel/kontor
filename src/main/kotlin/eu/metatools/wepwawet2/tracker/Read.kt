package eu.metatools.wepwawet2.tracker

import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.PropId

/**
 * Read of property [propId] on entity with identity [id].
 */
data class Read internal constructor(val id: Id, val propId: PropId)