import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class MinMaxTree(val root: Node) {
    sealed class Node {
        abstract val min: Int
        abstract val max: Int

        data class Branch(override val min: Int, override val max: Int, val left: Node, val right: Node?) : Node() {
            constructor(left: Node, right: Node?) : this(
                if (right == null) left.min else min(left.min, right.min),
                if (right == null) left.max else max(left.max, right.max),
                left, right
            )
        }
        data class Leaf(val value: Int) : Node() {
            override val min = value
            override val max = value
        }
    }

    companion object {
        fun construct(values: List<Int>): MinMaxTree {
            fun constructNode(values: List<Int>): Node =
                when (values.size) {
                    1 -> Node.Leaf(values[0])
//                    2 -> Node.Branch(constructNode(values.take(1)), constructNode(values.drop(1).take(1)))
                    else -> {
                        if (values.size.countOneBits() == 1 /* power of two */) {
                            val half = values.size / 2
                            Node.Branch(constructNode(values.take(half)), constructNode(values.drop(half)))
                        } else {
                            // 0010
                            // 4 - 2 = 2
                            // 1 << 2 = 100
                            val half = 1 shl (Int.SIZE_BITS - values.size.countLeadingZeroBits() - 1)
                            // TODO make this such that the degree of child nodes = parent degree - 1
                            Node.Branch(constructNode(values.take(half)), constructNode(values.drop(half)))
                        }
                    }
                }
            return MinMaxTree(constructNode(values))
        }
    }

    fun has(value: Int): Pair<Boolean, Int> {
        var steps = 0
        fun Node.has(value: Int): Boolean {
            steps++
            /*println("checking in node $this")*/
            return when {
                this is Node.Leaf && this.value == value -> true
                this is Node.Branch && this.min <= value && value <= this.max -> {
                    right!!
                    /*println("-> left half")*/
//                    left.has(value) || right?.also { /*println("-> right half")*/ }?.has(value) ?: false
                    if (Random.nextBoolean()) {
                        left.has(value) || right.has(value)
                    } else {
                        right.has(value) || left.has(value)
                    }
                }
                else -> {
                    /*println("X nothing found")*/
                    false
                }
            }
        }
        return root.has(value) to steps
    }
}

fun main() {
    val values = List(10_000) { Random.nextInt(0, 100_000) }.chunked(50).map { it.sorted() }.flatten()
    val tree = MinMaxTree.construct(values)

    val dataPairs = mutableListOf<Pair<Boolean, Int>>()

    values.forEach { value ->
        val pair = tree.has(value)
        dataPairs.add(pair)
        println(pair)
    }

    println(dataPairs.map { it.second }.average())

//    println(tree)
//    println()
    /*
    6              x
    5                 x
    4  x
    3        x
    2     x
    1           x
       0  1  2  3  4  5
     */
//    tree.has(1)
}