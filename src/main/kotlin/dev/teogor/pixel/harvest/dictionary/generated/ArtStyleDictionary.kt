package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val abstractSet = setOf(
    "abstract expressionism",
    "abstract impressionism",
    "abstract surrealism",
)

private val baroqueSet = setOf(
    "baroque revival",
    "late baroque",
)

private val cubistSet = setOf(
    "analytical cubism",
    "synthetic cubism",
)

private val futuristicSet = setOf(
    "cyberpunk",
    "steampunk",
)

private val geometricSet = setOf(
    "constructivism",
    "op art",
)

private val graffitiSet = setOf(
    "street art",
    "stencil graffiti",
)

private val illustrationSet = setOf(
    "digital illustration",
    "fantasy illustration",
)

private val minimalisticSet = setOf(
    "minimalism",
    "minimalist sculpture",
)

private val monochromeSet = setOf(
    "black and white photography",
    "monochromatic painting",
)

private val pastelSet = setOf(
    "pastel painting",
    "soft pastel drawing",
)

private val realismSet = setOf(
    "hyperrealism",
    "photorealism",
)

private val vectorSet = setOf(
    "vector art",
    "vector illustration",
)

private val watercolorSet = setOf(
    "landscape watercolor",
    "portrait watercolor",
)

class ArtStyleDictionary(list: Set<String>) : Dictionary(list) {
    class ArtStyleDictionaryBuilder : Builder()

    enum class ArtStyleTypes(private val artStyleSet: Set<String>) : Type {
        ABSTRACT(abstractSet),
        BAROQUE(baroqueSet),
        CUBIST(cubistSet),
        FUTURISTIC(futuristicSet),
        GEOMETRIC(geometricSet),
        GRAFFITI(graffitiSet),
        ILLUSTRATION(illustrationSet),
        MINIMALISTIC(minimalisticSet),
        MONOCHROME(monochromeSet),
        PASTEL(pastelSet),
        REALISM(realismSet),
        VECTOR(vectorSet),
        WATERCOLOR(watercolorSet),
        ALL(abstractSet + baroqueSet + cubistSet + futuristicSet + geometricSet + graffitiSet + illustrationSet + minimalisticSet + monochromeSet + pastelSet + realismSet + vectorSet + watercolorSet);

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
