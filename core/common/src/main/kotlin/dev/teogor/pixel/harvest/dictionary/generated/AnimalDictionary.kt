package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val mammalsSet = setOf(
    "aardvark",
    "anteater",
    "armadillo",
    "baboon",
    "badger",
    "bat",
    "bear",
    "beaver",
    "bison",
    "blackbird",
    "boar",
    "buffalo",
    "camel",
    "capybara",
    "caribou",
    "cat",
    "cheetah",
    "chimpanzee",
    "chinchilla",
    "clam",
    "coyote",
    "crab",
    "deer",
    "dinosaur",
    "dog",
    "dolphin",
    "donkey",
    "dugong",
    "elephant",
    "elk",
    "ferret",
    "finch",
    "fox",
    "gaur",
    "gazelle",
    "gerbil",
    "giant panda",
    "giraffe",
    "gnat",
    "gnu",
    "goat",
    "goldfinch",
    "gorilla",
    "grasshopper",
    "guinea pig",
    "hamster",
    "hare",
    "hedgehog",
    "hippopotamus",
    "hornet",
    "horse",
    "human",
    "hyena",
    "ibex",
    "jackal",
    "jaguar",
    "kangaroo",
    "koala",
    "kouprey",
    "kudu",
    "leopard",
    "lion",
    "llama",
    "lobster",
    "loris",
    "louse",
    "magpie",
    "mallard",
    "mandrill",
    "marten",
    "meerkat",
    "mink",
    "mole",
    "mongoose",
    "monkey",
    "moose",
    "mosquito",
    "mouse",
    "mule",
    "narwhal",
    "opossum",
    "orangutan",
    "oryx",
    "otter",
    "panther",
    "pig",
    "pigeon",
    "pony",
    "porcupine",
    "porpoise",
    "rabbit",
    "raccoon",
    "ram",
    "rat",
    "red deer",
    "red panda",
    "reindeer",
    "rhinoceros",
    "seal",
    "sheep",
    "shrew",
    "skunk",
    "snail",
    "sparrow",
    "squirrel",
    "tarsier",
    "tiger",
    "vampire bat",
    "wallaby",
    "walrus",
    "wasp",
    "weasel",
    "whale",
    "wildcat",
    "wolf",
    "wolverine",
    "wombat",
    "woodcock",
    "woodpecker",
)

private val birdsSet = setOf(
    "albatross",
    "cassowary",
    "chicken",
    "chough",
    "cormorant",
    "crow",
    "curlew",
    "dotterel",
    "dove",
    "eagle",
    "falcon",
    "fly",
    "gannet",
    "goose",
    "goshawk",
    "grouse",
    "guinea fowl",
    "gull",
    "hawk",
    "heron",
    "jay",
    "kingfisher",
    "kookaburra",
    "lapwing",
    "lark",
    "lyrebird",
    "ostrich",
    "owl",
    "parrot",
    "partridge",
    "peafowl",
    "pelican",
    "penguin",
    "pheasant",
    "quail",
    "quelea",
    "quetzal",
    "rail",
    "raven",
    "sandpiper",
    "starling",
    "stork",
    "swallow",
    "swan",
    "wren",
)

private val fishSet = setOf(
    "barracuda",
    "cod",
    "dogfish",
    "goldfish",
    "salmon",
    "sardine",
    "shark",
    "trout",
)

private val reptilesSet = setOf(
    "alligator",
    "cobra",
    "crocodile",
    "lizard",
    "snake",
    "viper",
)

private val amphibiansSet = setOf(
    "frog",
    "newt",
    "toad",
)

private val insectsSet = setOf(
    "ant",
    "bee",
    "butterfly",
    "caterpillar",
    "cicada",
    "cricket",
    "dragonfly",
    "ladybug",
    "moth",
    "scorpion",
    "spider",
    "termite",
)

class AnimalDictionary(list: Set<String>) : Dictionary(list) {
    class AnimalDictionaryBuilder : Builder()

    enum class AnimalTypes(private val animalSet: Set<String>) : Type {
        MAMMALS(mammalsSet),
        BIRDS(birdsSet),
        FISH(fishSet),
        REPTILES(reptilesSet),
        AMPHIBIANS(amphibiansSet),
        INSECTS(insectsSet),
        ALL(mammalsSet + birdsSet + fishSet + reptilesSet + amphibiansSet + insectsSet);

        override fun getSet(): Set<String> {
            return animalSet
        }
    }

    companion object {
        fun builder(block: AnimalDictionaryBuilder.() -> Unit): AnimalDictionary {
            val builder = AnimalDictionaryBuilder()
            builder.block()
            return AnimalDictionary(builder.build().list)
        }
    }

}