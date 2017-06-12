package eu.metatools.wepwawet.tools

/**
 * A function that also supports indexing.
 */
abstract class IndexFunction0<in I, out R> {
    /**
     * Executes the function without an index.
     */
    protected abstract fun exec(): R

    /**
     * Executes the function with an index.
     */
    protected abstract fun exec(index: I): R

    /**
     * Invokes the function without an index.
     */
    operator fun invoke(): R {
        return exec()
    }

    /**
     * Binds an index for later use.
     */
    operator fun get(index: I): () -> R {
        return { -> exec(index) }
    }
}

/**
 * Creates an [IndexFunction0].
 */
@JvmName("indexFunction0")
inline fun <I, R> indexFunction(
        crossinline execNoIndex: IndexFunction0<I, R>.() -> R,
        crossinline execIndex: IndexFunction0<I, R>.(I) -> R) =
        object : IndexFunction0<I, R>() {
            override fun exec(): R = execNoIndex()

            override fun exec(index: I) = execIndex(index)
        }

/**
 * A function that also supports indexing.
 */
abstract class IndexFunction1<in T, in I, out R> {
    /**
     * Executes the function without an index.
     */
    protected abstract fun exec(arg: T): R

    /**
     * Executes the function with an index.
     */
    protected abstract fun exec(arg: T, index: I): R

    /**
     * Invokes the function without an index.
     */
    operator fun invoke(arg: T): R {
        return exec(arg)
    }

    /**
     * Binds an index for later use.
     */
    operator fun get(index: I): (T) -> R {
        return { arg: T -> exec(arg, index) }
    }
}

/**
 * Creates an [IndexFunction1].
 */
@JvmName("indexFunction1")
inline fun <T, I, R> indexFunction(
        crossinline execNoIndex: IndexFunction1<T, I, R>.(T) -> R,
        crossinline execIndex: IndexFunction1<T, I, R>.(T, I) -> R) =
        object : IndexFunction1<T, I, R>() {
            override fun exec(arg: T) = execNoIndex(arg)

            override fun exec(arg: T, index: I) = execIndex(arg, index)
        }

/**
 * A function that also supports indexing.
 */
abstract class IndexFunction2<in T1, in T2, in I, out R> {
    /**
     * Executes the function without an index.
     */
    protected abstract fun exec(arg1: T1, arg2: T2): R

    /**
     * Executes the function with an index.
     */
    protected abstract fun exec(arg1: T1, arg2: T2, index: I): R

    /**
     * Invokes the function without an index.
     */
    operator fun invoke(arg1: T1, arg2: T2): R {
        return exec(arg1, arg2)
    }

    /**
     * Binds an index for later use.
     */
    operator fun get(index: I): (T1, T2) -> R {
        return { arg1: T1, arg2: T2 -> exec(arg1, arg2, index) }
    }
}

/**
 * Creates an [IndexFunction2].
 */
@JvmName("indexFunction2")
inline fun <T1, T2, I, R> indexFunction(
        crossinline execNoIndex: IndexFunction2<T1, T2, I, R>.(T1, T2) -> R,
        crossinline execIndex: IndexFunction2<T1, T2, I, R>.(T1, T2, I) -> R) =
        object : IndexFunction2<T1, T2, I, R>() {
            override fun exec(arg1: T1, arg2: T2) = execNoIndex(arg1, arg2)

            override fun exec(arg1: T1, arg2: T2, index: I) = execIndex(arg1, arg2, index)
        }