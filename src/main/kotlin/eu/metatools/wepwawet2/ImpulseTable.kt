package eu.metatools.wepwawet2

/**
 * Table of impulse identity to abstract impulse
 */
class ImpulseTable : MutableMap<PropId, Entity.(List<Any?>) -> Unit> by hashMapOf()