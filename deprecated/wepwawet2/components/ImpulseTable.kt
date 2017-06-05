package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.data.PropId

/**
 * Table of impulse identity to abstract impulse
 */
class ImpulseTable : MutableMap<PropId, Entity.(List<Any?>) -> Unit> by hashMapOf()