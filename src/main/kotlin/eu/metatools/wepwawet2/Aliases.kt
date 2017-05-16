package eu.metatools.wepwawet2

/**
 * Identity for entities.
 */
typealias Id = Int

/**
 * Casts to [Id].
 */
fun Number.toId(): Id = toInt()

/**
 * Identity for classes.
 */
typealias ClassId = Short

/**
 * Casts to [ClassId].
 */
fun Number.toClassId(): ClassId = toShort()

/**
 * Identity for constructors.
 */
typealias CtorId = Short

/**
 * Casts to [CtorId].
 */
fun Number.toCtorId(): CtorId = toShort()

/**
 * Identity for properties.
 */
typealias PropId = Int

/**
 * Casts to [PropId].
 */
fun Number.toPropId(): PropId = toInt()

/**
 * Time or revision.
 */
typealias Rev = Long

/**
 * Casts to [Rev].
 */
fun Number.toRev(): Rev = toLong()
