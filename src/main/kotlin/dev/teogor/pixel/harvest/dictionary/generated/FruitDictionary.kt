package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val citrusFruitsSet = setOf(
    "lemon",
    "lime",
    "orange",
    "grapefruit",
    "mandarin",
    "tangerine",
    "clementine",
    "bergamot",
    "kumquat",
    "yuzu",
    "calamondin",
    "pomelo",
    "citron",
    "ugli fruit",
    "bitter orange",
    "sudachi",
    "kaffir lime",
    "buddha's hand",
)

private val tropicalFruitsSet = setOf(
    "banana",
    "mango",
    "pineapple",
    "papaya",
    "kiwi",
    "coconut",
    "passion fruit",
    "dragon fruit",
    "guava",
    "lychee",
    "pomegranate",
    "avocado",
    "durian",
    "jackfruit",
    "mangosteen",
    "star fruit",
    "feijoa",
    "longan",
    "açaí",
    "soursop",
    "rambutan",
    "persimmon",
    "carambola",
)

private val berryFruitsSet = setOf(
    "strawberry",
    "blueberry",
    "raspberry",
    "blackberry",
    "cranberry",
    "gooseberry",
    "currant",
    "mulberry",
    "boysenberry",
    "elderberry",
    "huckleberry",
    "loganberry",
    "marionberry",
    "cloudberry",
    "bilberry",
    "strawberry guava",
    "salmonberry",
    "barberry",
    "juniper berry",
)

private val stoneFruitsSet = setOf(
    "peach",
    "plum",
    "apricot",
    "cherry",
    "nectarine",
    "quince",
    "mango (yes, it falls into both tropical and stone fruit categories)",
    "peach palm",
    "damson",
    "mirabelle",
    "greengage",
    "sloe",
    "loquat",
    "cherry plum",
    "blackthorn",
)

private val melonsSet = setOf(
    "watermelon",
    "cantaloupe",
    "honeydew melon",
    "galia melon",
    "casaba melon",
    "santa claus melon",
    "hami melon",
    "canary melon",
    "sprite melon",
    "juan canary melon",
    "golden langkawi melon",
    "tendral melon",
    "winter melon",
    "bitter melon",
    "piel de sapo melon",
    "gac fruit",
    "horned melon",
)

private val pomeFruitsSet = setOf(
    "apple",
    "pear",
    "quince",
    "crabapple",
    "medlar",
    "chayote",
    "hawthorn",
    "rowan",
    "serviceberry",
    "jujube",
)

private val exoticFruitsSet = setOf(
    "pitahaya (dragon fruit)",
    "ackee",
    "kiwano (horned melon)",
    "custard apple",
    "sapodilla",
    "breadfruit",
    "mamey sapote",
    "monstera deliciosa",
    "pepino melon",
    "atemoya",
    "mangaba",
    "feijoa",
    "guaje",
    "pulasan",
    "abiu",
    "sapote",
    "ilama",
    "jabuticaba",
    "surinam cherry",
    "salak (snake fruit)",
    "tamarillo",
    "canistel",
)

private val otherFruitsSet = setOf(
    "grapes",
    "fig",
    "olive",
    "kiwi berry",
    "pawpaw",
    "jambu",
    "lingonberry",
    "morus",
    "carob",
    "elderflower",
    "sea buckthorn",
    "camu camu",
    "chokeberry",
    "goumi",
    "bacuri",
    "cupuaçu",
    "bael",
    "roselle",
    "monkey fruit",
    "yangmei",
    "finger lime",
    "indian gooseberry",
    "mundu",
    "bignay",
    "cornelian cherry",
    "mabolo",
    "langsat",
    "malay apple",
    "caranda",
    "kiwiberry",
    "wampee",
    "white mulberry",
    "black mulberry",
    "chempedak",
    "cempedak",
    "bacupari",
    "wampi",
    "nam nam",
    "elephant apple",
)

class FruitDictionary(list: Set<String>) : Dictionary(list) {
    class FruitDictionaryBuilder : Builder()

    enum class FruitTypes(private val fruitSet: Set<String>) : Type {
        CITRUS_FRUITS(citrusFruitsSet),
        TROPICAL_FRUITS(tropicalFruitsSet),
        BERRY_FRUITS(berryFruitsSet),
        STONE_FRUITS(stoneFruitsSet),
        MELONS(melonsSet),
        POME_FRUITS(pomeFruitsSet),
        EXOTIC_FRUITS(exoticFruitsSet),
        OTHER_FRUITS(otherFruitsSet),
        ALL(citrusFruitsSet + tropicalFruitsSet + berryFruitsSet + stoneFruitsSet + melonsSet + pomeFruitsSet + exoticFruitsSet + otherFruitsSet);

        override fun getSet(): Set<String> {
            return fruitSet
        }
    }

    companion object {
        fun builder(block: FruitDictionaryBuilder.() -> Unit): FruitDictionary {
            val builder = FruitDictionaryBuilder()
            builder.block()
            return FruitDictionary(builder.build().list)
        }
    }

}
