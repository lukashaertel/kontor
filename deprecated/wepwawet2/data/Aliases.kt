package eu.metatools.wepwawet2.data

typealias ClassId = Short
fun Number.toClassId() = toShort()

typealias CreateId = Short
fun Number.toCtorId() = toShort()

typealias PropId = Int
fun Number.toPropId() = toInt()


typealias PerUpdate = Int
typealias PerImpulse = Short
typealias PerCreate = Short
typealias Resolution = Byte

typealias Id = Rev
