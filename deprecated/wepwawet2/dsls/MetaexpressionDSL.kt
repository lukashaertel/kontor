package eu.metatools.wepwawet2.dsls

/*
 * This file defines methods of creating expressions from function objects rather than explicit values.
 */

@JvmName("plusByte")
operator fun <R> (R.() -> Byte).plus(other: (R.() -> Byte)): R.() -> Int =
        { this@plus() + other() }

@JvmName("plusByte")
operator fun <R> (R.() -> Byte).plus(other: Byte): R.() -> Int =
        { this@plus() + other }

@JvmName("plusByte")
operator fun <R> Byte.plus(other: (R.() -> Byte)): R.() -> Int =
        { this@plus + other() }


@JvmName("minusByte")
operator fun <R> (R.() -> Byte).minus(other: (R.() -> Byte)): R.() -> Int =
        { this@minus() - other() }

@JvmName("minusByte")
operator fun <R> (R.() -> Byte).minus(other: Byte): R.() -> Int =
        { this@minus() - other }

@JvmName("minusByte")
operator fun <R> Byte.minus(other: (R.() -> Byte)): R.() -> Int =
        { this@minus - other() }


@JvmName("timesByte")
operator fun <R> (R.() -> Byte).times(other: (R.() -> Byte)): R.() -> Int =
        { this@times() * other() }

@JvmName("timesByte")
operator fun <R> (R.() -> Byte).times(other: Byte): R.() -> Int =
        { this@times() * other }

@JvmName("timesByte")
operator fun <R> Byte.times(other: (R.() -> Byte)): R.() -> Int =
        { this@times * other() }


@JvmName("divByte")
operator fun <R> (R.() -> Byte).div(other: (R.() -> Byte)): R.() -> Int =
        { this@div() / other() }

@JvmName("divByte")
operator fun <R> (R.() -> Byte).div(other: Byte): R.() -> Int =
        { this@div() / other }

@JvmName("divByte")
operator fun <R> Byte.div(other: (R.() -> Byte)): R.() -> Int =
        { this@div / other() }

@JvmName("remByte")
operator fun <R> (R.() -> Byte).rem(other: (R.() -> Byte)): R.() -> Int =
        { this@rem().rem(other()) }

@JvmName("remByte")
operator fun <R> (R.() -> Byte).rem(other: Byte): R.() -> Int =
        { this@rem().rem(other) }

@JvmName("remByte")
operator fun <R> Byte.rem(other: (R.() -> Byte)): R.() -> Int =
        { this@rem.rem(other()) }

@JvmName("ltByte")
infix fun <R> (R.() -> Byte).lt(other: (R.() -> Byte)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltByte")
infix fun <R> (R.() -> Byte).lt(other: Byte): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltByte")
infix fun <R> Byte.lt(other: (R.() -> Byte)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leByte")
infix fun <R> (R.() -> Byte).le(other: (R.() -> Byte)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leByte")
infix fun <R> (R.() -> Byte).le(other: Byte): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leByte")
infix fun <R> Byte.le(other: (R.() -> Byte)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqByte")
infix fun <R> (R.() -> Byte).eq(other: (R.() -> Byte)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqByte")
infix fun <R> (R.() -> Byte).eq(other: Byte): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqByte")
infix fun <R> Byte.eq(other: (R.() -> Byte)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neByte")
infix fun <R> (R.() -> Byte).ne(other: (R.() -> Byte)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neByte")
infix fun <R> (R.() -> Byte).ne(other: Byte): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neByte")
infix fun <R> Byte.ne(other: (R.() -> Byte)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geByte")
infix fun <R> (R.() -> Byte).ge(other: (R.() -> Byte)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geByte")
infix fun <R> (R.() -> Byte).ge(other: Byte): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geByte")
infix fun <R> Byte.ge(other: (R.() -> Byte)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtByte")
infix fun <R> (R.() -> Byte).gt(other: (R.() -> Byte)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtByte")
infix fun <R> (R.() -> Byte).gt(other: Byte): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtByte")
infix fun <R> Byte.gt(other: (R.() -> Byte)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@JvmName("plusShort")
operator fun <R> (R.() -> Short).plus(other: (R.() -> Short)): R.() -> Int =
        { this@plus() + other() }

@JvmName("plusShort")
operator fun <R> (R.() -> Short).plus(other: Short): R.() -> Int =
        { this@plus() + other }

@JvmName("plusShort")
operator fun <R> Short.plus(other: (R.() -> Short)): R.() -> Int =
        { this@plus + other() }


@JvmName("minusShort")
operator fun <R> (R.() -> Short).minus(other: (R.() -> Short)): R.() -> Int =
        { this@minus() - other() }

@JvmName("minusShort")
operator fun <R> (R.() -> Short).minus(other: Short): R.() -> Int =
        { this@minus() - other }

@JvmName("minusShort")
operator fun <R> Short.minus(other: (R.() -> Short)): R.() -> Int =
        { this@minus - other() }


@JvmName("timesShort")
operator fun <R> (R.() -> Short).times(other: (R.() -> Short)): R.() -> Int =
        { this@times() * other() }

@JvmName("timesShort")
operator fun <R> (R.() -> Short).times(other: Short): R.() -> Int =
        { this@times() * other }

@JvmName("timesShort")
operator fun <R> Short.times(other: (R.() -> Short)): R.() -> Int =
        { this@times * other() }


@JvmName("divShort")
operator fun <R> (R.() -> Short).div(other: (R.() -> Short)): R.() -> Int =
        { this@div() / other() }

@JvmName("divShort")
operator fun <R> (R.() -> Short).div(other: Short): R.() -> Int =
        { this@div() / other }

@JvmName("divShort")
operator fun <R> Short.div(other: (R.() -> Short)): R.() -> Int =
        { this@div / other() }

@JvmName("remShort")
operator fun <R> (R.() -> Short).rem(other: (R.() -> Short)): R.() -> Int =
        { this@rem().rem(other()) }

@JvmName("remShort")
operator fun <R> (R.() -> Short).rem(other: Short): R.() -> Int =
        { this@rem().rem(other) }

@JvmName("remShort")
operator fun <R> Short.rem(other: (R.() -> Short)): R.() -> Int =
        { this@rem.rem(other()) }

@JvmName("ltShort")
infix fun <R> (R.() -> Short).lt(other: (R.() -> Short)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltShort")
infix fun <R> (R.() -> Short).lt(other: Short): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltShort")
infix fun <R> Short.lt(other: (R.() -> Short)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leShort")
infix fun <R> (R.() -> Short).le(other: (R.() -> Short)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leShort")
infix fun <R> (R.() -> Short).le(other: Short): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leShort")
infix fun <R> Short.le(other: (R.() -> Short)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqShort")
infix fun <R> (R.() -> Short).eq(other: (R.() -> Short)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqShort")
infix fun <R> (R.() -> Short).eq(other: Short): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqShort")
infix fun <R> Short.eq(other: (R.() -> Short)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neShort")
infix fun <R> (R.() -> Short).ne(other: (R.() -> Short)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neShort")
infix fun <R> (R.() -> Short).ne(other: Short): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neShort")
infix fun <R> Short.ne(other: (R.() -> Short)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geShort")
infix fun <R> (R.() -> Short).ge(other: (R.() -> Short)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geShort")
infix fun <R> (R.() -> Short).ge(other: Short): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geShort")
infix fun <R> Short.ge(other: (R.() -> Short)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtShort")
infix fun <R> (R.() -> Short).gt(other: (R.() -> Short)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtShort")
infix fun <R> (R.() -> Short).gt(other: Short): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtShort")
infix fun <R> Short.gt(other: (R.() -> Short)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


@JvmName("plusInt")
operator fun <R> (R.() -> Int).plus(other: (R.() -> Int)): R.() -> Int =
        { this@plus() + other() }

@JvmName("plusInt")
operator fun <R> (R.() -> Int).plus(other: Int): R.() -> Int =
        { this@plus() + other }

@JvmName("plusInt")
operator fun <R> Int.plus(other: (R.() -> Int)): R.() -> Int =
        { this@plus + other() }


@JvmName("minusInt")
operator fun <R> (R.() -> Int).minus(other: (R.() -> Int)): R.() -> Int =
        { this@minus() - other() }

@JvmName("minusInt")
operator fun <R> (R.() -> Int).minus(other: Int): R.() -> Int =
        { this@minus() - other }

@JvmName("minusInt")
operator fun <R> Int.minus(other: (R.() -> Int)): R.() -> Int =
        { this@minus - other() }


@JvmName("timesInt")
operator fun <R> (R.() -> Int).times(other: (R.() -> Int)): R.() -> Int =
        { this@times() * other() }

@JvmName("timesInt")
operator fun <R> (R.() -> Int).times(other: Int): R.() -> Int =
        { this@times() * other }

@JvmName("timesInt")
operator fun <R> Int.times(other: (R.() -> Int)): R.() -> Int =
        { this@times * other() }


@JvmName("divInt")
operator fun <R> (R.() -> Int).div(other: (R.() -> Int)): R.() -> Int =
        { this@div() / other() }

@JvmName("divInt")
operator fun <R> (R.() -> Int).div(other: Int): R.() -> Int =
        { this@div() / other }

@JvmName("divInt")
operator fun <R> Int.div(other: (R.() -> Int)): R.() -> Int =
        { this@div / other() }

@JvmName("remInt")
operator fun <R> (R.() -> Int).rem(other: (R.() -> Int)): R.() -> Int =
        { this@rem().rem(other()) }

@JvmName("remInt")
operator fun <R> (R.() -> Int).rem(other: Int): R.() -> Int =
        { this@rem().rem(other) }

@JvmName("remInt")
operator fun <R> Int.rem(other: (R.() -> Int)): R.() -> Int =
        { this@rem.rem(other()) }

@JvmName("ltInt")
infix fun <R> (R.() -> Int).lt(other: (R.() -> Int)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltInt")
infix fun <R> (R.() -> Int).lt(other: Int): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltInt")
infix fun <R> Int.lt(other: (R.() -> Int)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leInt")
infix fun <R> (R.() -> Int).le(other: (R.() -> Int)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leInt")
infix fun <R> (R.() -> Int).le(other: Int): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leInt")
infix fun <R> Int.le(other: (R.() -> Int)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqInt")
infix fun <R> (R.() -> Int).eq(other: (R.() -> Int)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqInt")
infix fun <R> (R.() -> Int).eq(other: Int): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqInt")
infix fun <R> Int.eq(other: (R.() -> Int)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neInt")
infix fun <R> (R.() -> Int).ne(other: (R.() -> Int)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neInt")
infix fun <R> (R.() -> Int).ne(other: Int): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neInt")
infix fun <R> Int.ne(other: (R.() -> Int)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geInt")
infix fun <R> (R.() -> Int).ge(other: (R.() -> Int)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geInt")
infix fun <R> (R.() -> Int).ge(other: Int): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geInt")
infix fun <R> Int.ge(other: (R.() -> Int)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtInt")
infix fun <R> (R.() -> Int).gt(other: (R.() -> Int)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtInt")
infix fun <R> (R.() -> Int).gt(other: Int): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtInt")
infix fun <R> Int.gt(other: (R.() -> Int)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


@JvmName("plusLong")
operator fun <R> (R.() -> Long).plus(other: (R.() -> Long)): R.() -> Long =
        { this@plus() + other() }

@JvmName("plusLong")
operator fun <R> (R.() -> Long).plus(other: Long): R.() -> Long =
        { this@plus() + other }

@JvmName("plusLong")
operator fun <R> Long.plus(other: (R.() -> Long)): R.() -> Long =
        { this@plus + other() }


@JvmName("minusLong")
operator fun <R> (R.() -> Long).minus(other: (R.() -> Long)): R.() -> Long =
        { this@minus() - other() }

@JvmName("minusLong")
operator fun <R> (R.() -> Long).minus(other: Long): R.() -> Long =
        { this@minus() - other }

@JvmName("minusLong")
operator fun <R> Long.minus(other: (R.() -> Long)): R.() -> Long =
        { this@minus - other() }


@JvmName("timesLong")
operator fun <R> (R.() -> Long).times(other: (R.() -> Long)): R.() -> Long =
        { this@times() * other() }

@JvmName("timesLong")
operator fun <R> (R.() -> Long).times(other: Long): R.() -> Long =
        { this@times() * other }

@JvmName("timesLong")
operator fun <R> Long.times(other: (R.() -> Long)): R.() -> Long =
        { this@times * other() }


@JvmName("divLong")
operator fun <R> (R.() -> Long).div(other: (R.() -> Long)): R.() -> Long =
        { this@div() / other() }

@JvmName("divLong")
operator fun <R> (R.() -> Long).div(other: Long): R.() -> Long =
        { this@div() / other }

@JvmName("divLong")
operator fun <R> Long.div(other: (R.() -> Long)): R.() -> Long =
        { this@div / other() }

@JvmName("remLong")
operator fun <R> (R.() -> Long).rem(other: (R.() -> Long)): R.() -> Long =
        { this@rem().rem(other()) }

@JvmName("remLong")
operator fun <R> (R.() -> Long).rem(other: Long): R.() -> Long =
        { this@rem().rem(other) }

@JvmName("remLong")
operator fun <R> Long.rem(other: (R.() -> Long)): R.() -> Long =
        { this@rem.rem(other()) }

@JvmName("ltLong")
infix fun <R> (R.() -> Long).lt(other: (R.() -> Long)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltLong")
infix fun <R> (R.() -> Long).lt(other: Long): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltLong")
infix fun <R> Long.lt(other: (R.() -> Long)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leLong")
infix fun <R> (R.() -> Long).le(other: (R.() -> Long)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leLong")
infix fun <R> (R.() -> Long).le(other: Long): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leLong")
infix fun <R> Long.le(other: (R.() -> Long)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqLong")
infix fun <R> (R.() -> Long).eq(other: (R.() -> Long)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqLong")
infix fun <R> (R.() -> Long).eq(other: Long): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqLong")
infix fun <R> Long.eq(other: (R.() -> Long)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neLong")
infix fun <R> (R.() -> Long).ne(other: (R.() -> Long)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neLong")
infix fun <R> (R.() -> Long).ne(other: Long): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neLong")
infix fun <R> Long.ne(other: (R.() -> Long)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geLong")
infix fun <R> (R.() -> Long).ge(other: (R.() -> Long)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geLong")
infix fun <R> (R.() -> Long).ge(other: Long): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geLong")
infix fun <R> Long.ge(other: (R.() -> Long)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtLong")
infix fun <R> (R.() -> Long).gt(other: (R.() -> Long)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtLong")
infix fun <R> (R.() -> Long).gt(other: Long): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtLong")
infix fun <R> Long.gt(other: (R.() -> Long)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


@JvmName("plusFloat")
operator fun <R> (R.() -> Float).plus(other: (R.() -> Float)): R.() -> Float =
        { this@plus() + other() }

@JvmName("plusFloat")
operator fun <R> (R.() -> Float).plus(other: Float): R.() -> Float =
        { this@plus() + other }

@JvmName("plusFloat")
operator fun <R> Float.plus(other: (R.() -> Float)): R.() -> Float =
        { this@plus + other() }


@JvmName("minusFloat")
operator fun <R> (R.() -> Float).minus(other: (R.() -> Float)): R.() -> Float =
        { this@minus() - other() }

@JvmName("minusFloat")
operator fun <R> (R.() -> Float).minus(other: Float): R.() -> Float =
        { this@minus() - other }

@JvmName("minusFloat")
operator fun <R> Float.minus(other: (R.() -> Float)): R.() -> Float =
        { this@minus - other() }


@JvmName("timesFloat")
operator fun <R> (R.() -> Float).times(other: (R.() -> Float)): R.() -> Float =
        { this@times() * other() }

@JvmName("timesFloat")
operator fun <R> (R.() -> Float).times(other: Float): R.() -> Float =
        { this@times() * other }

@JvmName("timesFloat")
operator fun <R> Float.times(other: (R.() -> Float)): R.() -> Float =
        { this@times * other() }


@JvmName("divFloat")
operator fun <R> (R.() -> Float).div(other: (R.() -> Float)): R.() -> Float =
        { this@div() / other() }

@JvmName("divFloat")
operator fun <R> (R.() -> Float).div(other: Float): R.() -> Float =
        { this@div() / other }

@JvmName("divFloat")
operator fun <R> Float.div(other: (R.() -> Float)): R.() -> Float =
        { this@div / other() }

@JvmName("remFloat")
operator fun <R> (R.() -> Float).rem(other: (R.() -> Float)): R.() -> Float =
        { this@rem().rem(other()) }

@JvmName("remFloat")
operator fun <R> (R.() -> Float).rem(other: Float): R.() -> Float =
        { this@rem().rem(other) }

@JvmName("remFloat")
operator fun <R> Float.rem(other: (R.() -> Float)): R.() -> Float =
        { this@rem.rem(other()) }

@JvmName("ltFloat")
infix fun <R> (R.() -> Float).lt(other: (R.() -> Float)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltFloat")
infix fun <R> (R.() -> Float).lt(other: Float): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltFloat")
infix fun <R> Float.lt(other: (R.() -> Float)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leFloat")
infix fun <R> (R.() -> Float).le(other: (R.() -> Float)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leFloat")
infix fun <R> (R.() -> Float).le(other: Float): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leFloat")
infix fun <R> Float.le(other: (R.() -> Float)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqFloat")
infix fun <R> (R.() -> Float).eq(other: (R.() -> Float)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqFloat")
infix fun <R> (R.() -> Float).eq(other: Float): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqFloat")
infix fun <R> Float.eq(other: (R.() -> Float)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neFloat")
infix fun <R> (R.() -> Float).ne(other: (R.() -> Float)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neFloat")
infix fun <R> (R.() -> Float).ne(other: Float): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neFloat")
infix fun <R> Float.ne(other: (R.() -> Float)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geFloat")
infix fun <R> (R.() -> Float).ge(other: (R.() -> Float)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geFloat")
infix fun <R> (R.() -> Float).ge(other: Float): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geFloat")
infix fun <R> Float.ge(other: (R.() -> Float)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtFloat")
infix fun <R> (R.() -> Float).gt(other: (R.() -> Float)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtFloat")
infix fun <R> (R.() -> Float).gt(other: Float): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtFloat")
infix fun <R> Float.gt(other: (R.() -> Float)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


@JvmName("plusDouble")
operator fun <R> (R.() -> Double).plus(other: (R.() -> Double)): R.() -> Double =
        { this@plus() + other() }

@JvmName("plusDouble")
operator fun <R> (R.() -> Double).plus(other: Double): R.() -> Double =
        { this@plus() + other }

@JvmName("plusDouble")
operator fun <R> Double.plus(other: (R.() -> Double)): R.() -> Double =
        { this@plus + other() }


@JvmName("minusDouble")
operator fun <R> (R.() -> Double).minus(other: (R.() -> Double)): R.() -> Double =
        { this@minus() - other() }

@JvmName("minusDouble")
operator fun <R> (R.() -> Double).minus(other: Double): R.() -> Double =
        { this@minus() - other }

@JvmName("minusDouble")
operator fun <R> Double.minus(other: (R.() -> Double)): R.() -> Double =
        { this@minus - other() }


@JvmName("timesDouble")
operator fun <R> (R.() -> Double).times(other: (R.() -> Double)): R.() -> Double =
        { this@times() * other() }

@JvmName("timesDouble")
operator fun <R> (R.() -> Double).times(other: Double): R.() -> Double =
        { this@times() * other }

@JvmName("timesDouble")
operator fun <R> Double.times(other: (R.() -> Double)): R.() -> Double =
        { this@times * other() }


@JvmName("divDouble")
operator fun <R> (R.() -> Double).div(other: (R.() -> Double)): R.() -> Double =
        { this@div() / other() }

@JvmName("divDouble")
operator fun <R> (R.() -> Double).div(other: Double): R.() -> Double =
        { this@div() / other }

@JvmName("divDouble")
operator fun <R> Double.div(other: (R.() -> Double)): R.() -> Double =
        { this@div / other() }

@JvmName("remDouble")
operator fun <R> (R.() -> Double).rem(other: (R.() -> Double)): R.() -> Double =
        { this@rem().rem(other()) }

@JvmName("remDouble")
operator fun <R> (R.() -> Double).rem(other: Double): R.() -> Double =
        { this@rem().rem(other) }

@JvmName("remDouble")
operator fun <R> Double.rem(other: (R.() -> Double)): R.() -> Double =
        { this@rem.rem(other()) }

@JvmName("ltDouble")
infix fun <R> (R.() -> Double).lt(other: (R.() -> Double)): R.() -> Boolean =
        { this@lt() < other() }

@JvmName("ltDouble")
infix fun <R> (R.() -> Double).lt(other: Double): R.() -> Boolean =
        { this@lt() < other }

@JvmName("ltDouble")
infix fun <R> Double.lt(other: (R.() -> Double)): R.() -> Boolean =
        { this@lt < other() }

@JvmName("leDouble")
infix fun <R> (R.() -> Double).le(other: (R.() -> Double)): R.() -> Boolean =
        { this@le() <= other() }

@JvmName("leDouble")
infix fun <R> (R.() -> Double).le(other: Double): R.() -> Boolean =
        { this@le() <= other }

@JvmName("leDouble")
infix fun <R> Double.le(other: (R.() -> Double)): R.() -> Boolean =
        { this@le <= other() }

@JvmName("eqDouble")
infix fun <R> (R.() -> Double).eq(other: (R.() -> Double)): R.() -> Boolean =
        { this@eq() == other() }

@JvmName("eqDouble")
infix fun <R> (R.() -> Double).eq(other: Double): R.() -> Boolean =
        { this@eq() == other }

@JvmName("eqDouble")
infix fun <R> Double.eq(other: (R.() -> Double)): R.() -> Boolean =
        { this@eq == other() }

@JvmName("neDouble")
infix fun <R> (R.() -> Double).ne(other: (R.() -> Double)): R.() -> Boolean =
        { this@ne() != other() }

@JvmName("neDouble")
infix fun <R> (R.() -> Double).ne(other: Double): R.() -> Boolean =
        { this@ne() != other }

@JvmName("neDouble")
infix fun <R> Double.ne(other: (R.() -> Double)): R.() -> Boolean =
        { this@ne != other() }

@JvmName("geDouble")
infix fun <R> (R.() -> Double).ge(other: (R.() -> Double)): R.() -> Boolean =
        { this@ge() >= other() }

@JvmName("geDouble")
infix fun <R> (R.() -> Double).ge(other: Double): R.() -> Boolean =
        { this@ge() >= other }

@JvmName("geDouble")
infix fun <R> Double.ge(other: (R.() -> Double)): R.() -> Boolean =
        { this@ge >= other() }

@JvmName("gtDouble")
infix fun <R> (R.() -> Double).gt(other: (R.() -> Double)): R.() -> Boolean =
        { this@gt() > other() }

@JvmName("gtDouble")
infix fun <R> (R.() -> Double).gt(other: Double): R.() -> Boolean =
        { this@gt() > other }

@JvmName("gtDouble")
infix fun <R> Double.gt(other: (R.() -> Double)): R.() -> Boolean =
        { this@gt > other() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@JvmName("unaryMinusByte")
operator fun <R> (R.() -> Byte).unaryMinus(): R.() -> Int =
        { -this@unaryMinus() }

@JvmName("unaryMinusShort")
operator fun <R> (R.() -> Short).unaryMinus(): R.() -> Int =
        { -this@unaryMinus() }

@JvmName("unaryMinusInt")
operator fun <R> (R.() -> Int).unaryMinus(): R.() -> Int =
        { -this@unaryMinus() }

@JvmName("unaryMinusLong")
operator fun <R> (R.() -> Long).unaryMinus(): R.() -> Long =
        { -this@unaryMinus() }

@JvmName("unaryMinusFloat")
operator fun <R> (R.() -> Float).unaryMinus(): R.() -> Float =
        { -this@unaryMinus() }

@JvmName("unaryMinusDouble")
operator fun <R> (R.() -> Double).unaryMinus(): R.() -> Double =
        { -this@unaryMinus() }


operator fun <R> (R.() -> Boolean).not(): R.() -> Boolean =
        { !this@not() }

infix fun <R> (R.() -> Boolean).and(other: () -> Boolean): R.() -> Boolean =
        { this@and() && other() }

infix fun <R> (R.() -> Boolean).and(other: Boolean): R.() -> Boolean =
        { this@and() && other }

infix fun <R> Boolean.and(other: () -> Boolean): R.() -> Boolean =
        { this@and && other() }

infix fun <R> (R.() -> Boolean).or(other: () -> Boolean): R.() -> Boolean =
        { this@or() || other() }

infix fun <R> (R.() -> Boolean).or(other: Boolean): R.() -> Boolean =
        { this@or() || other }

infix fun <R> Boolean.or(other: () -> Boolean): R.() -> Boolean =
        { this@or || other() }

fun <R> (R.() -> Number).toByte(): R.() -> Byte =
        { this@toByte().toByte() }

fun <R> (R.() -> Number).toShort(): R.() -> Short =
        { this@toShort().toShort() }

fun <R> (R.() -> Number).toInt(): R.() -> Int =
        { this@toInt().toInt() }

fun <R> (R.() -> Number).toLong(): R.() -> Long =
        { this@toLong().toLong() }

fun <R> (R.() -> Number).toFloat(): R.() -> Float =
        { this@toFloat().toFloat() }

fun <R> (R.() -> Number).toDouble(): R.() -> Double =
        { this@toDouble().toDouble() }
