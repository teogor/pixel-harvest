package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val list = setOf(
    "art deco",
    "artistic",
    "avant-garde",
    "beachy",
    "bohemian",
    "boho chic",
    "boho glam",
    "boho",
    "casual chic",
    "casual",
    "chic",
    "classic",
    "contemporary minimalist",
    "contemporary",
    "country",
    "eclectic",
    "edgy glam",
    "edgy",
    "elegant",
    "folk",
    "formal",
    "gothic",
    "grunge",
    "grunge-inspired",
    "hipster",
    "industrial",
    "minimalist",
    "modern classic",
    "modern",
    "nautical",
    "preppy",
    "punk",
    "retro glam",
    "retro",
    "retro-inspired",
    "rocker",
    "romantic",
    "romantic vintage",
    "scandinavian",
    "sophisticated",
    "sporty luxe",
    "sporty",
    "street",
    "tribal",
    "urban bohemian",
    "urban",
    "vintage",
    "vintage-inspired",
    "western",
    "whimsical",
)

class StyleDictionary(list: Set<String>) : Dictionary(list) {
    class StyleDictionaryBuilder : Builder()

    enum class StyleTypes(private val styleSet: Set<String>) : Type {
        ALL(list);

        override fun getSet(): Set<String> {
            return styleSet
        }
    }
    companion object {
        fun builder(block: StyleDictionaryBuilder.() -> Unit): StyleDictionary {
            val builder = StyleDictionaryBuilder()
            builder.block()
            return StyleDictionary(builder.build().list)
        }
    }

}
