package eu.metatools.wepwawet.universe

import eu.metatools.kontor.serialization.ByteBufInput
import eu.metatools.kontor.serialization.ByteBufOutput
import eu.metatools.kontor.serialization.serializerOf
import eu.metatools.wepwawet.Wepwawet
import eu.metatools.wepwawet.components.MapRevisionTable
import eu.metatools.wepwawet.net.Net
import io.netty.buffer.ByteBufAllocator
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.serialization.Serializable
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.serialization.*
import java.io.Reader
import kotlin.serialization.internal.IntSerializer

@Serializable
data class Stuff(val x: Number)

data class WrappedClass(val kClass: KClass<*>) {
    companion object : KSerializer<WrappedClass> {
        override fun save(output: KOutput, obj: WrappedClass) {
            output.writeStringValue(obj.kClass.javaObjectType.name)
        }

        override fun load(input: KInput): WrappedClass {
            return WrappedClass(Class.forName(input.readStringValue()).kotlin)
        }

        override val serializableClass: KClass<*>
            get() = WrappedClass::class
    }
}

fun main(args: Array<String>) {

    val x = JSON.stringify(Stuff(123))
    println(x)
    val y = JSON.parse<Stuff>(x)
    println(y)

    /**
    class A(wepwawet: Wepwawet, i: Int) {
    val x: Int = i
    var y: Short = (-i).toShort()
    }

    class B(wepwawet: Wepwawet, i: Int, d: Double) {
    var s: String = "$i"
    var v: Float = d.toFloat()
    }

    val v = Int::class.serializer()

    val m = NumericUniverse(universeEntry(::A), universeEntry(::B))

    val x = JSON.stringify(m.classSerializer, A::class)
    println(x)
    val y = JSON.parse(m.universeEntrySerializer, x)
    println(y)
    val w = Wepwawet(MapRevisionTable(), hashMapOf(), Net(Unit, Channel(), Channel()))
    println(y(w, 10))*/
}