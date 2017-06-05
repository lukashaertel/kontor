package eu.metatools.wepwawet.tracking

/**
 * A trace from call to all dependencies and the nested traces.
 */
data class Trace<out R>(
        val call: Call,
        val result: R,
        val dependencies: Set<Dep>,
        val nested: Set<Trace<*>>) {
    /**
     * Returns all dependencies in this trace and all nested traces
     */
    val allDependencies: Set<Dep> =
            nested.fold(dependencies) { a, b -> a union b.allDependencies }

    private fun stats(indent: Int) {
        print("  ".repeat(indent))
        println("$call -> $result from")
        for (s in dependencies) {
            print("  ".repeat(indent))
            println("  $s")
        }
        if (nested.isNotEmpty()) {
            print("  ".repeat(indent))
            println("  called:")
            for (n in nested)
                n.stats(indent + 1)
        }
    }

    fun stats() {
        stats(0)
    }
}