package dev.teogor.pixel.harvest.dictionary

private val fundamentalColors = setOf(
    "Red",
    "Pink",
    "Purple",
    "Deep Purple",
    "Indigo",
    "Blue",
    "Light Blue",
    "Cyan",
    "Teal",
    "Green",
    "Light Green",
    "Lime",
    "Yellow",
    "Amber",
    "Orange",
    "Deep Orange",
    "Brown",
    "Grey",
    "Blue Grey"
)

private val neutralColors = setOf(
    "Black",
    "White"
)

class ColorDictionary(list: Set<String>) : Dictionary(list) {
    open class ColorDictionaryBuilder : Builder()

    enum class ColorTypes(private val colorSet: Set<String>) : Type {
        FUNDAMENTAL(fundamentalColors),
        NEUTRAL(neutralColors),
        ALL(fundamentalColors + neutralColors);

        override fun getSet(): Set<String> {
            return colorSet
        }
    }

    companion object {
        fun builder(block: ColorDictionaryBuilder.() -> Unit): ColorDictionary {
            val builder = ColorDictionaryBuilder()
            builder.block()
            return ColorDictionary(builder.build().list)
        }
    }
}
