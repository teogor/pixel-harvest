package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val africanEthnicitiesSet = setOf(
    "akan",
    "amhara",
    "ashanti",
    "bambara",
    "berber",
    "beti",
    "bubi",
    "dinka",
    "fang",
    "fulani",
    "hausa",
    "igbo",
    "kikuyu",
    "luba",
    "malinke",
    "mandinka",
    "masai",
    "ndebele",
    "oromo",
    "peul",
    "shona",
    "somali",
    "swahili",
    "tigrinya",
    "tuareg",
    "xhosa",
    "yoruba",
    "zulu",
)

private val asianEthnicitiesSet = setOf(
    "arab",
    "bengali",
    "burmese",
    "chinese",
    "filipino",
    "hindi",
    "indonesian",
    "japanese",
    "javanese",
    "korean",
    "malay",
    "malayali",
    "mongolian",
    "pakistani",
    "punjabi",
    "sinhalese",
    "tamil",
    "thai",
    "turkish",
    "uyghur",
    "uzbek",
    "vietnamese",
)

private val europeanEthnicitiesSet = setOf(
    "basque",
    "catalan",
    "danish",
    "dutch",
    "english",
    "finnish",
    "french",
    "german",
    "greek",
    "hungarian",
    "irish",
    "italian",
    "norwegian",
    "polish",
    "portuguese",
    "russian",
    "scottish",
    "serbian",
    "spanish",
    "swedish",
    "ukrainian",
)

private val northAmericanEthnicitiesSet = setOf(
    "african american",
    "cajun",
    "cree",
    "haitian",
    "inuit",
    "irish american",
    "mexican american",
    "navajo",
    "puerto rican",
    "seminole",
)

private val oceanianEthnicitiesSet = setOf(
    "aboriginal australian",
    "kanak",
    "maori",
    "melanesian",
    "micronesian",
    "polynesian",
    "torres strait islander",
)

private val southAmericanEthnicitiesSet = setOf(
    "aymara",
    "guarani",
    "inca",
    "mapuche",
    "quechua",
    "wayuu",
)

private val otherEthnicitiesSet = setOf(
    "black",
    "white",
    "european",
    "asian",
    "caucasian",
    "hispanic",
    "latino",
)

class EthnicityDictionary(list: Set<String>) : Dictionary(list) {
    class EthnicityDictionaryBuilder : Builder()

    enum class EthnicityTypes(private val ethnicitySet: Set<String>) : Type {
        AFRICAN_ETHNICITIES(africanEthnicitiesSet),
        ASIAN_ETHNICITIES(asianEthnicitiesSet),
        EUROPEAN_ETHNICITIES(europeanEthnicitiesSet),
        NORTH_AMERICAN_ETHNICITIES(northAmericanEthnicitiesSet),
        OCEANIAN_ETHNICITIES(oceanianEthnicitiesSet),
        SOUTH_AMERICAN_ETHNICITIES(southAmericanEthnicitiesSet),
        OTHER_ETHNICITIES(otherEthnicitiesSet),
        ALL(africanEthnicitiesSet + asianEthnicitiesSet + europeanEthnicitiesSet + northAmericanEthnicitiesSet + oceanianEthnicitiesSet + southAmericanEthnicitiesSet + otherEthnicitiesSet);

        override fun getSet(): Set<String> {
            return ethnicitySet
        }
    }

    companion object {
        fun builder(block: EthnicityDictionaryBuilder.() -> Unit): EthnicityDictionary {
            val builder = EthnicityDictionaryBuilder()
            builder.block()
            return EthnicityDictionary(builder.build().list)
        }
    }

}
