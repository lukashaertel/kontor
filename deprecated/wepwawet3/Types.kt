package eu.metatools.wepwawet

import com.google.common.collect.ComparisonChain
import com.google.common.hash.HashCode

/**
 * Revision with internal tracking numbers and origin byte.
 */
data class Rev(val major: Int, val minor: Short, val origin: Byte) : Comparable<Rev> {
    override fun compareTo(other: Rev) = ComparisonChain.start()
            .compare(major, other.major)
            .compare(minor, other.minor)
            .compare(origin, other.origin)
            .result()

    companion object {
        fun upperBoundOf(rev: Rev) = Rev(rev.major, Short.MAX_VALUE, Byte.MAX_VALUE)

        fun lowerBoundOf(rev: Rev) = Rev(rev.major, 0, 0)
    }

    /**
     * Sets the major version and resets the minor version.
     */
    fun setMajor(to: Int) =
            copy(major = to, minor = 0)

    /**
     * Increases the major version and resets the minor version.
     */
    fun incMajor() =
            copy(major = major.inc(), minor = 0)

    /**
     * Increases the minor version.
     */
    fun incMinor() =
            copy(minor = minor.inc())

    override fun toString() =
            if (minor == Short.MAX_VALUE && origin == Byte.MAX_VALUE)
                "sup $major"
            else if (minor == 0.toShort() && origin == 0.toByte())
                "inf $major"
            else
                "$major.$minor/$origin"
}

/**
 * An ID, currently substituted by revision.
 */
data class Id(val rev: Rev, val sub: Short) : Comparable<Id> {
    override fun compareTo(other: Id) = ComparisonChain.start()
            .compare(rev, other.rev)
            .compare(sub, other.sub)
            .result()

    override fun toString() = "$rev#$sub"
}


/**
 * The identity of a constructor.
 */
data class ConstructorId(val classNum: Short, val constructorNum: Byte)

/**
 * The identity of a member.
 */
data class MemberId(val classNum: Short, val memberNum: Byte)

/**
 * A call to [memberId] on entity [id] with argument [arg].
 */
data class Call(
        val id: Id,
        val memberId: MemberId,
        val arg: Any?)

/**
 * Tracked dependencies.
 */
data class Deps(
        val operates: List<Id>,
        val reads: List<Pair<Id, MemberId>>,
        val writes: List<Pair<Id, MemberId>>,
        val constructs: List<Id>,
        val deletes: List<Id>) {

    /**
     * True if the receiver invalidates [other].
     */
    infix fun invalidates(other: Deps) =
            writes.any { it in other.reads } || deletes.any { it in other.operates }

    /**
     * True if the receiver precursors [other].
     */
    infix fun precursors(other: Deps) =
            writes.any { it in other.reads } || constructs.any { it in other.operates }

}