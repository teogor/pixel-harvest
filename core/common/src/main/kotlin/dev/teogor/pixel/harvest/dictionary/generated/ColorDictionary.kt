package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val fundamentalColorsSet = setOf(
    "red",
    "pink",
    "purple",
    "deep purple",
    "indigo",
    "blue",
    "light blue",
    "cyan",
    "teal",
    "green",
    "light green",
    "lime",
    "yellow",
    "amber",
    "orange",
    "deep orange",
    "brown",
    "grey",
    "blue grey",
)

private val neutralColorsSet = setOf(
    "black",
    "white",
)

class ColorDictionary(list: Set<String>) : Dictionary(list) {
    class ColorDictionaryBuilder : Builder()

    enum class ColorTypes(private val colorSet: Set<String>) : Type {
        FUNDAMENTAL_COLORS(fundamentalColorsSet),
        NEUTRAL_COLORS(neutralColorsSet),
        ALL(fundamentalColorsSet + neutralColorsSet);

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
