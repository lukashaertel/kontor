package eu.metatools.wepwawet2

/**
 * Table of property to reactors
 */
class ReactTable {
    /**
     * Internal backing multi-map
     */
    private val backing = hashMapOf<PropId, MutableSet<Entity.() -> Unit>>()

    /**
     * Registers the block with the dependencies.
     */
    fun register(depends: List<PropId>, block: Entity.() -> Unit) {
        for (depend in depends)
            backing.getOrPut(depend, { hashSetOf() }).add(block)
    }

    /**
     * Finds all reactors by touched properties.
     */
    fun find(touched: Iterable<PropId>) = touched
            .mapNotNull { backing[it] }
            .flatten()
            .distinct()
}