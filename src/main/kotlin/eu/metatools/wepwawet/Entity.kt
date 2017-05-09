package eu.metatools.wepwawet

/**
 * Entity registered in a [Wepwawet] container, exposing the [id] as an cross-network and cross-game identifying
 * feature.
 */
interface Entity {
    /**
     * The parent container, instances of implementing classes must use [Wepwawet.obtain] to get a registered and
     * undoable instance.
     */
    val parent: Wepwawet

    /**
     * The identity of the object, it will be filled in by [Wepwawet.obtain], which provides a non-colliding ID.
     */
    val id: Int
}

/**
 * Releases the receiver entity.
 */
fun Entity.release() {
    parent.release(this)
}