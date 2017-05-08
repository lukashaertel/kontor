package eu.metatools.wepwawet.tracking

/**
 * A trace from call to all dependencies and the nested traces.
 */
data class Trace<C, D>(
        val call: C,
        val dependencies: Set<D>,
        val nested: Set<Trace<C, D>>) {
    /**
     * Returns all dependencies in this trace and all nested traces
     */
    val allDependencies: Set<D> =
            nested.fold(dependencies) { a, b -> a union b.allDependencies }
}