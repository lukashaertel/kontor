package eu.metatools.common

import kotlinx.serialization.SerialContext
import kotlinx.serialization.internal.*

val primitiveSerialContext = SerialContext().apply {
    registerSerializer(Long::class, LongSerializer)
    registerSerializer(Boolean::class, BooleanSerializer)
    registerSerializer(Unit::class, UnitSerializer)
    registerSerializer(Short::class, ShortSerializer)
    registerSerializer(Char::class, CharSerializer)
    registerSerializer(String::class, StringSerializer)
    registerSerializer(Byte::class, ByteSerializer)
    registerSerializer(Float::class, FloatSerializer)
    registerSerializer(Int::class, IntSerializer)
}