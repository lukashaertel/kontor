package eu.metatools.wepwawet2.data


/**
 * Read or write of property [propId] on entity with identity [id].
 */
data class Access internal constructor(val id: Id, val propId: PropId)