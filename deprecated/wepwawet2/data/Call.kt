package eu.metatools.wepwawet2.data

import eu.metatools.wepwawet2.util.intersects


/**
 * A call net on entity [id]'s impulse [propId] with arguments [args],
 */
data class Call(
        val id: Id,
        val propId: PropId,
        val args: List<Any?>,
        val reads: List<Access> = emptyList(),
        val writes: List<Access> = emptyList(),
        val creates: List<Id> = emptyList(),
        val demands: List<Id> = emptyList(),
        val evicts: List<Id> = emptyList()) {
    /**
     * If the receiver is earlier than the [other] call, returns true if the receiver requires reevaluation of [other].
     */
    infix fun invalidates(other: Call) =
            writes intersects other.reads || evicts intersects other.demands

    override fun toString() = buildString {
        append(id)
        append('.')
        append(propId)
        append('(')
        append(args.joinToString { "$it" })
        append(')')
        if (creates.isNotEmpty()) {
            append(" +")
            append(creates.joinToString { "$it" })
        }
        if (demands.isNotEmpty()) {
            append(" ^")
            append(demands.joinToString { "$it" })
        }

        if (evicts.isNotEmpty()) {
            append(" -")
            append(evicts.joinToString { "$it" })
        }
    }
}

