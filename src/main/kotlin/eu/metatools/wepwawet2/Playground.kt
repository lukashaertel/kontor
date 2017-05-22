package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.components.AssociateMapper
import eu.metatools.wepwawet2.components.DummyNet
import eu.metatools.wepwawet2.dsls.gt
import eu.metatools.wepwawet2.dsls.impulse
import eu.metatools.wepwawet2.dsls.prop
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class Nimbus(override val node: Node) : Entity {
    var money by prop(100)

    val cost by prop(20)

    val excessiveDrinking by impulse { ->
        money -= cost
        println("Sufff!!")
    }

    override fun toString() = "Nimbus(money=$money, cost=$cost)"
}

fun main(args: Array<String>) {
    val a = AssociateMapper(Nimbus::class)
    val n1 = DummyNet()
    val n2 = DummyNet(Any(), n1.outbound, n1.inbound)

    val w1 = Wepwawet(a, n1)
    val w2 = Wepwawet(a, n2)

    launch(CommonPool) {
        while (true) {
            w2.testReceive()
            println(w2.entityTable.entries)
        }
    }

    val e1 = w1.create(::Nimbus)
    val e2 = w2.create(::Nimbus)
    e1.excessiveDrinking()
    e1.excessiveDrinking()
    e1.excessiveDrinking()

    runBlocking { delay(1000) }

}