package eu.metatools.wepwawet

import com.google.common.collect.ComparisonChain

/**
 * Revision with author in [eu.metatools.rome.Repo].
 */
data class Revision(
        val time: Int,
        val author: Byte) : Comparable<Revision> {
    override fun compareTo(other: Revision) = ComparisonChain.start()
            .compare(time, other.time)
            .compare(author, other.author)
            .result()
}