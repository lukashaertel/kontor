package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.components.AssociateMapper
import eu.metatools.wepwawet2.components.DummyNet
import eu.metatools.wepwawet2.dsls.gt
import eu.metatools.wepwawet2.dsls.impulse
import eu.metatools.wepwawet2.dsls.prop

class Nimbus(override val node: Node) : Entity {
    var money by prop(100)

    val cost by prop(20)

    val excessiveDrinking by impulse(Nimbus::money gt Nimbus::cost) { ->
        money -= 20
        println("Sufff!!")
    }
}

fun main(args: Array<String>) {
    val w = Wepwawet(AssociateMapper(Nimbus::class), DummyNet())
    val n = w.create(::Nimbus)
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
    n.excessiveDrinking()
}