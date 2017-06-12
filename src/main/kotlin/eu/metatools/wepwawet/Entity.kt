package eu.metatools.wepwawet

import eu.metatools.rome.Action
import eu.metatools.wepwawet.tools.IndexFunction0
import eu.metatools.wepwawet.tools.IndexFunction1
import eu.metatools.wepwawet.tools.IndexFunction2
import eu.metatools.wepwawet.tools.indexFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

/**
 * Boxed value to support null values in maps.
 */
private data class Box<out T>(val value: T)

/**
 * Shared undoing state. If this is true, undoing is active.
 */
private val undoing = ThreadLocal.withInitial { false }

/**
 * Shared tracking state. If this is true, tracking is active.
 */
private val tracking = ThreadLocal.withInitial { false }

/**
 * Current assignments.
 */
private val assigns = ThreadLocal<MutableMap<BoundProperty<*, *>, Box<Any?>>>()

/**
 * Current entity creations as set of identities.
 */
private val creates = ThreadLocal<MutableSet<Entity>>()

/**
 * Current deletes as set of deleted entities.
 */
private val deletes = ThreadLocal<MutableSet<Entity>>()

/**
 * A property bound to it's receiver.
 */
private data class BoundProperty<T : Entity, R>(
        val receiver: T,
        val property: KMutableProperty1<T, R>)


/**
 * A undo batch.
 */
private data class EntityUndo(
        val unAssign: Map<BoundProperty<*, *>, Box<Any?>>,
        val unCreate: Set<Entity>,
        val unDelete: Set<Entity>)

/**
 * Auto generated key components for entities.
 */
enum class AutoKeyMode {
    /**
     * Do not generate a key, this can be used for data only entities with no impulses.
     */
    NONE,

    /**
     * Generate key from class name.
     */
    PER_CLASS,
}

/**
 * An entity in [container] with [id].
 */
abstract class Entity(val container: Container, val autoKeyMode: AutoKeyMode = AutoKeyMode.PER_CLASS) {

    /**
     * Local impulse table for resolution of external calls.
     */
    private val table = arrayListOf<(Any?) -> Unit>()

    /**
     * Local key table for key computation.
     */
    private val keys = when (autoKeyMode) {
        AutoKeyMode.NONE -> arrayListOf<() -> Any?>()
        AutoKeyMode.PER_CLASS -> {
            val a = javaClass.simpleName
            arrayListOf<() -> Any?>({ a })
        }
    }

    /**
     * Returns true if the entity has a key.
     */
    internal fun hasKey() = keys.isNotEmpty()

    /**
     * Computes the current entity key.
     */
    internal fun primaryKey() = keys.map { it() }


    /**
     * Computes the run action for the call to [call] on argument [arg].
     */
    internal fun runAction(call: Byte, arg: Any?): Action<Revision, *> {
        return object : Action<Revision, EntityUndo> {
            override fun exec(time: Revision): EntityUndo {
                // Clear tracker
                assigns.set(hashMapOf())
                creates.set(hashSetOf())
                deletes.set(hashSetOf())

                // Activate tracking
                tracking.set(true)

                // Call block
                table[call.toInt()](arg)

                // Deactivate tracking
                tracking.set(false)

                // Return undo
                return EntityUndo(assigns.get(), creates.get(), deletes.get())
            }

            override fun undo(time: Revision, carry: EntityUndo) {
                // Activate undoing
                undoing.set(true)

                for ((k, v) in carry.unAssign)
                    @Suppress("unchecked_cast")
                    (k.property as KMutableProperty1<Entity, Any?>).set(k.receiver, v.value)

                for (i in carry.unCreate)
                    container.unregisterEntity(i)

                for (e in carry.unDelete)
                    container.registerEntity(e)

                // Deactivate undoing
                undoing.set(false)
            }

        }
    }

    /**
     * Tracks setting of a property, must be called before assigned.
     */
    private fun trackSet(property: KMutableProperty1<Entity, Any?>) {
        // Check tracking validity
        if (!tracking.get() && !undoing.get())
            throw IllegalStateException("Setting from non-tracking or undoing area.")

        // Put previous value, without overwriting.
        if (!undoing.get())
            assigns.get().computeIfAbsent(BoundProperty(this, property), { Box(property.get(this)) })
    }

    /**
     * Creates an entity with the given construcotor and the given id.
     */
    protected fun <E : Entity> create(constructor: (Container) -> E): E {
        // Validate call location
        if (!tracking.get() && !undoing.get())
            throw IllegalStateException("Creating from non-tracking or undoing area.")

        // Execute and add to container
        val e = constructor(container)
        container.registerEntity(e)

        // Track create if not undoing
        if (!undoing.get())
            creates.get().add(e)

        return e
    }

    protected fun <E : Entity, T> create(constructor: (Container, T) -> E, t: T) =
            create { constructor(it, t) }

    protected fun <E : Entity, T, U> create(constructor: (Container, T, U) -> E, t: T, u: U) =
            create { constructor(it, t, u) }

    /**
     * Deletes the entity.
     */
    protected fun delete(entity: Entity) {
        // Validate call location
        if (!tracking.get() && !undoing.get())
            throw IllegalStateException("Deleting from non-tracking or undoing area.")

        // Track delete if not undoing
        if (!undoing.get())
            deletes.get().add(entity)

        // Execute and remove
        container.unregisterEntity(entity)
    }

    /**
     * Formats the members for [toString].
     */
    open fun toStringMembers() = ""

    override fun toString() = toStringMembers().let {
        if (it.isNotEmpty())
            "[${javaClass.simpleName}, key=${primaryKey()}, $it]"
        else
            "[${javaClass.simpleName}, key=${primaryKey()}]"
    }

    /**
     * A proxy for an entity, i.e., to be resolved by the container.
     */
    private data class EntityProxy(val id: List<Any?>)

    /**
     * Tries to convert to a proxy or returns identity.
     */
    private fun tryToProxy(any: Any?) =
            if (any is Entity)
                toProxy(any)
            else
                any

    /**
     * Tries to convert from a proxy or returns identity.
     */
    private fun tryFromProxy(any: Any?) =
            if (any is EntityProxy)
                fromProxy(any)
            else
                any


    /**
     * Converts to a proxy.
     */
    private fun toProxy(entity: Entity) =
            EntityProxy(entity.primaryKey())

    /**
     * Converts from a proxy.
     */
    private fun fromProxy(entityProxy: EntityProxy) =
            container.find(entityProxy.id)

    /**
     * A tracking property identifying the entity.
     */
    private class Key<T>(initial: T, val delta: (T, T) -> Unit) : Delegate<Entity, T> {
        /**
         * Internal storage field.
         */
        internal var status = initial

        override fun getValue(r: Entity, p: KProperty<*>): T {
            return status
        }

        @Suppress("unchecked_cast")
        override fun setValue(r: Entity, p: KProperty<*>, value: T) {
            // Skip non-mutation
            if (value == status) return

            // Remove from old identity
            r.container.unregisterEntity(r)

            // Track set
            r.trackSet(p as KMutableProperty1<Entity, Any?>)

            // Write status
            val prev = status
            status = value

            // Add to new identity
            r.container.registerEntity(r)

            // Execute delta
            delta(prev, value)
        }
    }

    /**
     * Creates and registers a key. If [T] is an entity, the [primaryKey] of the value will be used instead.
     */
    protected fun <T> key(initial: T, delta: (T, T) -> Unit) = Provider { r: Entity, _ ->
        // Return key, also add the internal getter to the key providers
        Key(initial, delta).also {
            r.keys.add {
                it.status.let {
                    if (it is Entity)
                        it.primaryKey()
                    else
                        it
                }
            }
        }
    }

    protected fun <T> key(initial: T) =
            key(initial) { _, _ -> }

    /**
     * A tracking property.
     */
    private class Property<T>(initial: T, val delta: (T, T) -> Unit) : Delegate<Entity, T> {
        /**
         * Internal storage field.
         */
        internal var status = initial

        override fun getValue(r: Entity, p: KProperty<*>): T {
            return status
        }

        @Suppress("unchecked_cast")
        override fun setValue(r: Entity, p: KProperty<*>, value: T) {
            // Skip non-mutation
            if (value == status) return

            // Track set
            r.trackSet(p as KMutableProperty1<Entity, Any?>)

            // Write status
            val prev = status
            status = value

            // Execute delta
            delta(prev, value)
        }
    }

    /**
     * Creates a tracking property with a delta reactor.
     */
    protected fun <T> prop(initial: T, delta: (T, T) -> Unit): Delegate<Entity, T> =
            Property(initial, delta)

    /**
     * Creates a tracking property.
     */
    protected fun <T> prop(initial: T) =
            prop(initial) { _, _ -> }

    /**
     * Creates a single entity container that can be nullable. On value change, non-contained entities will be
     * removed. When not given explicitly, the property will be initialized with null.
     */
    protected fun <T : Entity> holdOptional(initial: T? = null, delta: (T?, T?) -> Unit) =
            prop(initial) { x, y ->
                delta(x, y)
                if (!undoing.get())
                    if (x != null)
                        delete(x)

            }

    /**
     * Creates a single entity container that can be nullable. On value change, non-contained entities will be
     * removed. When not given explicitly, the property will be initialized with null.
     */
    protected fun <T : Entity> holdOptional(initial: T? = null) =
            holdOptional(initial) { _, _ -> }

    /**
     * Creates a single entity container. On value change, non-contained entities will be removed.
     */
    protected fun <T : Entity> holdOne(initial: T, delta: (T, T) -> Unit) =
            prop(initial) { x, y ->
                delta(x, y)
                if (!undoing.get())
                    delete(x)
            }

    /**
     * Creates a single entity container. On value change, non-contained entities will be removed.
     */
    protected fun <T : Entity> holdOne(initial: T) =
            holdOne(initial) { _, _ -> }

    /**
     * Creates a many object container. On value change, non-contained entities will be removed. When not given
     * explicitly, the property will be initialized with an empty list.
     */
    protected fun <T : Entity> holdMany(initial: List<T> = listOf(), delta: (List<T>, List<T>) -> Unit) =
            prop(initial) { xs, ys ->
                delta(xs, ys)
                if (!undoing.get())
                    for (x in xs)
                        if (x !in ys)
                            delete(x)
            }

    protected fun <T : Entity> holdMany(initial: List<T> = listOf()) =
            holdMany(initial) { _, _ -> }

    /**
     * Utility function for time substitution used in delayed impulses.
     */
    private inline fun untrackedOffsetRun(time: Int, block: () -> Unit) {
        // Store and change tracking and container time
        val prevTracking = tracking.get()
        val prevTime = container.time

        tracking.set(false)
        container.time = time

        block()

        // Restore
        container.time = prevTime
        tracking.set(prevTracking)
    }

    /**
     * An impulse without arguments.
     */
    private class UnitImpulse<in R : Entity>(
            val call: Byte,
            val block: R.() -> Unit) : Delegate<R, IndexFunction0<Double, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<Double, Unit>({ ->
            // Check that key is present
            if (!r.hasKey())
                throw IllegalStateException("Cannot call impulse entity without key.")

            // Compute key
            val key = r.primaryKey()

            if (tracking.get())
                r.block()
            else {
                r.container.receive(r.container.rev(), key, call, Unit)
                r.container.dispatch(r.container.rev(), key, call, Unit)
                r.container.incInner()
            }
        }, { delay ->
            val ms = (delay * 1000.0).toInt()
            if (ms < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (ms == 0)
                this()
            else
                r.untrackedOffsetRun(r.container.time + ms) {
                    this()
                }
        })
    }

    /**
     * Creates and registers an impulse.
     */
    @Suppress("unchecked_cast")
    @JvmName("unitImpulse")
    protected fun <R : Entity> R.impulse(block: R.() -> Unit) = Provider { r: R, p ->
        if (p is KMutableProperty<*>)
            throw IllegalArgumentException("Impulses cannot be mutable fields.")

        r.table.add { _ ->
            block()
        }

        UnitImpulse((r.table.size - 1).toByte(), block)
    }

    /**
     * An impulse with one argument. Entity arguments will be dispatched via their [primaryKey].
     */
    private class Impulse<in R : Entity, T>(
            val call: Byte,
            val block: R.(T) -> Unit) : Delegate<R, IndexFunction1<T, Double, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<T, Double, Unit>({ t ->
            // Check that key is present
            if (!r.hasKey())
                throw IllegalStateException("Cannot call impulse entity without key.")

            // Compute key
            val key = r.primaryKey()

            if (tracking.get())
                r.block(t)
            else {
                val arg = r.tryToProxy(t)
                r.container.receive(r.container.rev(), key, call, arg)
                r.container.dispatch(r.container.rev(), key, call, arg)
                r.container.incInner()
            }
        }, { t, delay ->
            val ms = (delay * 1000.0).toInt()
            if (ms < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (ms == 0)
                this(t)
            else
                r.untrackedOffsetRun(r.container.time + ms) {
                    this(t)
                }
        })
    }

    /**
     * Creates and registers an impulse. Entity arguments will be dispatched via their [primaryKey].
     */
    @Suppress("unchecked_cast")
    @JvmName("impulse")
    protected fun <R : Entity, T> R.impulse(block: R.(T) -> Unit) = Provider { r: R, p ->
        if (p is KMutableProperty<*>)
            throw IllegalArgumentException("Impulses cannot be mutable fields.")

        r.table.add { a ->
            block(tryFromProxy(a) as T)
        }

        Impulse((r.table.size - 1).toByte(), block)
    }

    /**
     * An impulse with two arguments. Entity arguments will be dispatched via their [primaryKey].
     */
    private class BiImpulse<in R : Entity, T, U>(
            val call: Byte,
            val block: R.(T, U) -> Unit) : Delegate<R, IndexFunction2<T, U, Double, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<T, U, Double, Unit>({ t, u ->
            // Check that key is present
            if (!r.hasKey())
                throw IllegalStateException("Cannot call impulse entity without key.")

            // Compute key
            val key = r.primaryKey()

            if (tracking.get())
                r.block(t, u)
            else {
                val arg = r.tryToProxy(t) to r.tryToProxy(u)
                r.container.receive(r.container.rev(), key, call, arg)
                r.container.dispatch(r.container.rev(), key, call, arg)
                r.container.incInner()
            }
        }, { t, u, delay ->
            val ms = (delay * 1000.0).toInt()
            if (ms < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (ms == 0)
                this(t, u)
            else
                r.untrackedOffsetRun(r.container.time + ms) {
                    this(t, u)
                }
        })
    }

    /**
     * Creates and registers an impulse. Entity arguments will be dispatched via their [primaryKey].
     */
    @Suppress("unchecked_cast")
    @JvmName("biImpulse")
    protected fun <R : Entity, T, U> R.impulse(block: R.(T, U) -> Unit) = Provider { r: R, p ->
        if (p is KMutableProperty<*>)
            throw IllegalArgumentException("Impulses cannot be mutable fields.")

        r.table.add { a ->
            val arg = a as Pair<*, *>
            val t = tryFromProxy(arg.first) as T
            val u = tryFromProxy(arg.second) as U
            block(t, u)
        }

        BiImpulse((r.table.size - 1).toByte(), block)
    }
}
