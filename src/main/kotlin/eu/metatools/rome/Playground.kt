package eu.metatools.rome

fun main(args: Array<String>) {
    var status = 0

    fun set(to: Int) = object : Action<Int, Int> {
        override fun exec(time: Int) = status.also {
            status = to
        }

        override fun undo(time: Int, carry: Int) {
            status = carry
        }
    }

    fun add(n: Int) = object : Action<Int, Unit> {
        override fun exec(time: Int) {
            status += n
        }

        override fun undo(time: Int, carry: Unit) {
            status -= n
        }
    }

    fun sub(n: Int) = object : Action<Int, Unit> {
        override fun exec(time: Int) {
            status -= n
        }

        override fun undo(time: Int, carry: Unit) {
            status += n
        }
    }

    fun mul(n: Int) = if (n == 0)
        object : Action<Int, Int> {
            override fun exec(time: Int) = status.also {
                status = 0
            }

            override fun undo(time: Int, carry: Int) {
                status = carry
            }
        }
    else
        object : Action<Int, Unit> {
            override fun exec(time: Int) {
                status *= n
            }

            override fun undo(time: Int, carry: Unit) {
                status /= n
            }
        }


    val r = Repo<Int>()
    r.insert(add(40), 10)
    r.insert(mul(2), 20)
    // 40 * 2 = 80

    println(status)
    r.insert(sub(5), 5)
    // (40-5) * 2 = 70

    println(status)
    r.insert(set(5), 15)
    // 5 * 2 = 10

    println(status)

}