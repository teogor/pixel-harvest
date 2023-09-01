package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val shapesSet = setOf(
    "abstract form shape",
    "circle shape",
    "curve shape",
    "diamond shape",
    "geometric shape",
    "hexagon shape",
    "irregular polygon shape",
    "loop shape",
    "octagon shape",
    "oval shape",
    "pentagon shape",
    "rectangle shape",
    "spiral shape",
    "square shape",
    "star shape",
    "swirl shape",
    "symmetrical figure shape",
    "triangle shape",
    "wave shape",
    "zigzag shape",
)

class ShapeDictionary(list: Set<String>) : Dictionary(list) {
    class ShapeDictionaryBuilder : Builder()

    enum class ShapeTypes(private val shapeSet: Set<String>) : Type {
        SHAPES(shapesSet),
        ALL(shapesSet);

        override fun getSet(): Set<String> {
            return shapeSet
        }
    }

    companion object {
        fun builder(block: ShapeDictionaryBuilder.() -> Unit): ShapeDictionary {
            val builder = ShapeDictionaryBuilder()
            builder.block()
            return ShapeDictionary(builder.build().list)
        }
    }

}
