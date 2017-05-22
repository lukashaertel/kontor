package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.tools.cast

/**
 * A node, i.e., the raw entity.
 */
data class Node(val container: Wepwawet, val id: Id, var revTable: RevTable) {
    /**
     * Sets the value for [propId] at the current revision.
     */
    fun setValue(propId: PropId, value: Any?) {
        // Track and set
        container.tracker.write(id, propId)
        revTable.setAt(propId, container.rev, value)
    }

    /**
     * Gets and casts the value of [propId] at the current revision.
     */
    fun <T> getValue(propId: PropId): T {
        container.tracker.read(id, propId)
        return cast(revTable.getLatest(propId, container.rev))
    }

}