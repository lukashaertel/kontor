package eu.metatools.wepwawet2.tracker

import eu.metatools.wepwawet2.Args
import eu.metatools.wepwawet2.CtorId
import eu.metatools.wepwawet2.Id

/**
 * Tracks destruction of entities with given identity [id].
 */
data class Dtor internal constructor(val id: Id)