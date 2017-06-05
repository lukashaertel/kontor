package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.data.Rev

/**
 * Interface of components that support revision sign-off.
 */
interface SignOff {
    /**
     * Sign off up, **but not including**, this revision.
     */
    fun signOff(rev: Rev)
}