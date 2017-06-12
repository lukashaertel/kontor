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
        AutoKeyMode.NONE -> arrayListOf<() -> Any>()
        AutoKeyMode.PER_CLASS -> {
            val a = javaClass.simpleName
            arrayListOf<() -> Any>({ a })
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
    private data class EntityProxy(val id: List<Any>)

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
    private class Key<in R : Entity, T>(initial: T) : Delegate<R, T> {
        /**
         * Internal storage field.
         */
        internal var status = initial

        override fun getValue(r: R, p: KProperty<*>): T {
            return status
        }

        @Suppress("unchecked_cast")
        override fun setValue(r: R, p: KProperty<*>, value: T) {
            // Skip non-mutation
            if (value == status) return

            // Remove from old identity
            r.container.unregisterEntity(r)

            // Track set
            r.trackSet(p as KMutableProperty1<Entity, Any?>)

            // Write status
            status = value

            // Add to new identity
            r.container.registerEntity(r)
        }
    }

    /**
     * Creates and registers a key. If [T] is an entity, the [primaryKey] of the value will be used instead.
     */
    @Suppress("unchecked_cast")
    protected fun <R : Entity, T : Any> R.key(initial: T) = Provider { r: R, _ ->
        // Return key, also add the internal getter to the key providers

        Key<R, T>(initial).also {
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

    /**
     * A tracking property.
     */
    private class Property<in R : Entity, T>(initial: T) : Delegate<R, T> {
        /**
         * Internal storage field.
         */
        internal var status = initial

        override fun getValue(r: R, p: KProperty<*>): T {
            return status
        }

        @Suppress("unchecked_cast")
        override fun setValue(r: R, p: KProperty<*>, value: T) {
            // Skip non-mutation
            if (value == status) return

            // Track set
            r.trackSet(p as KMutableProperty1<Entity, Any?>)

            // Write status
            status = value
        }
    }

    /**
     * Creates a tracking property.
     */
    protected fun <R : Entity, T> R.prop(initial: T): Delegate<R, T> =
            Property(initial)

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
            val block: R.() -> Unit) : Delegate<R, IndexFunction0<Int, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<Int, Unit>({ ->
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
            if (delay < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (delay == 0)
                this()
            else
                r.untrackedOffsetRun(r.container.time + delay) {
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
            val block: R.(T) -> Unit) : Delegate<R, IndexFunction1<T, Int, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<T, Int, Unit>({ t ->
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
            if (delay < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (delay == 0)
                this(t)
            else
                r.untrackedOffsetRun(r.container.time + delay) {
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
            val block: R.(T, U) -> Unit) : Delegate<R, IndexFunction2<T, U, Int, Unit>> {
        override fun getValue(r: R, p: KProperty<*>) = indexFunction<T, U, Int, Unit>({ t, u ->
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
            if (delay < 0)
                throw IllegalArgumentException("Cannot use negative delay.")
            else if (delay == 0)
                this(t, u)
            else
                r.untrackedOffsetRun(r.container.time + delay) {
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
