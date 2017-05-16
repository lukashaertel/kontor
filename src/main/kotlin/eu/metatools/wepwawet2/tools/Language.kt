package eu.metatools.wepwawet2.tools

/**
 * For each argument [ts] applies [block].
 */
inline fun <T> each(vararg ts: T, block: T.() -> Unit) {
    for (t in ts) t.block()
}

/**
 * Casts to a generic [T], suppressing checkedness issues.
 */
fun <T> cast(any: Any?): T {
    @Suppress("unchecked_cast")
    val r = any as T
    return r
}