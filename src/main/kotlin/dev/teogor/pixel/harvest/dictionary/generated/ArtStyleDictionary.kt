package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val list = setOf(
    "abstract",
    "baroque",
    "cubist",
    "flat",
    "futuristic",
    "geometric",
    "graffiti",
    "illustration",
    "minimalistic",
    "monochrome",
    "pastel",
    "vector",
    "watercolor",
)

class ArtStyleDictionary(list: Set<String>) : Dictionary(list) {
    class ArtStyleDictionaryBuilder : Builder()

    enum class ArtStyleTypes(private val artStyleSet: Set<String>) : Type {
        ALL(list);

        override fun getSet(): Set<String> {
            return artStyleSet
        }
    }
    companion object {
        fun builder(block: ArtStyleDictionaryBuilder.() -> Unit): ArtStyleDictionary {
            val builder = ArtStyleDictionaryBuilder()
            builder.block()
            return ArtStyleDictionary(builder.build().list)
        }
    }

}
