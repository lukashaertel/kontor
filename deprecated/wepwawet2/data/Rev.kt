package eu.metatools.wepwawet2.data

import eu.metatools.wepwawet2.util.compare
import eu.metatools.wepwawet2.util.thenCompare

/**
 * Time or revision.
 */
data class Rev(
        val perUpdate: PerUpdate,
        val perImpulse: PerImpulse,
        val perCreate: PerCreate,
        val resolution: Resolution) : Comparable<Rev> {

    override fun compareTo(other: Rev) =
            compare {
                perUpdate to other.perUpdate
            } thenCompare {
                perImpulse to other.perImpulse
            } thenCompare {
                perCreate to other.perCreate
            } thenCompare {
                resolution to other.resolution
            }

    override fun toString() = "$perUpdate.$perImpulse.$perCreate/$resolution"
}