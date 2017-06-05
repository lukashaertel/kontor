package eu.metatools.wepwawet2.util

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.Wepwawet
import eu.metatools.wepwawet2.data.Call
import eu.metatools.wepwawet2.data.Rev

private fun StringBuilder.formatEntity(now: Rev, entity: Entity) {
    appendln("For entity $entity")
    for ((p, vs) in entity.node.backing) {
        append("  ")
        append(entity.node.container.mapper.unmapProp(p).name)
        append(" = ")
        appendln(vs.floorEntry(now)?.value)
    }
    appendln("For ETAB")
    for (i in entity.node.container.entityTable.idsAt(now)) {
        append("  ")
        appendln(i)
    }

}

private fun StringBuilder.formatCall(now: Rev, wepwawet: Wepwawet, call: Call) {
    append("For call ")
    append(wepwawet.entityTable.getAt(now, call.id))
    append('.')
    append(wepwawet.mapper.unmapProp(call.propId).name)
    append('(')
    append(call.args.joinToString { it.toString() })
    append(')')
    if (call.creates.isNotEmpty()) {
        append(" +")
        append(call.creates.map { (id) -> id }.joinToString { "$it" })
    }
    if (call.evicts.isNotEmpty()) {
        append(" -")
        append(call.evicts.map { (id) -> id }.joinToString { "$it" })
    }
    appendln()

}

class WepwawetException(val now: Rev, message: String, throwable: Throwable? = null) :
        Exception(format(now, message), throwable) {
    companion object {
        private fun format(now: Rev, message: String) = "At $now: $message"
    }
}

class WepwawetEntityException(val now: Rev, val entity: Entity,
                              message: String, throwable: Throwable? = null) :
        Exception(format(now, entity, message), throwable) {
    companion object {
        private fun format(now: Rev, entity: Entity, message: String) = buildString {
            appendln("At $now: $message")
            formatEntity(now, entity)
        }

    }
}

class WepwawetCallException(val now: Rev, val wepwawet: Wepwawet, val call: Call,
                            message: String, throwable: Throwable? = null) :
        Exception(format(now, wepwawet, call, message), throwable) {
    companion object {
        private fun format(now: Rev, wepwawet: Wepwawet, call: Call, message: String) = buildString {
            appendln("At $now: $message")
            formatCall(now, wepwawet, call)
        }
    }
}

class WepwawetEntityCallException(val now: Rev, val entity: Entity, val call: Call,
                                  message: String, throwable: Throwable? = null) :
        Exception(format(now, entity, call, message), throwable) {
    companion object {
        private fun format(now: Rev, entity: Entity, call: Call, message: String) = buildString {
            appendln("At $now: $message")
            formatCall(now, entity.node.container, call)
            formatEntity(now, entity)
        }
    }
}
