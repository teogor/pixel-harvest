package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val seasonsSet = setOf(
    "spring",
    "summer",
    "autumn/fall",
    "winter",
)

private val monthsSet = setOf(
    "january",
    "february",
    "march",
    "april",
    "may",
    "june",
    "july",
    "august",
    "september",
    "october",
    "november",
    "december",
)

private val zodiacSignsSet = setOf(
    "aries (march 21 - april 19)",
    "taurus (april 20 - may 20)",
    "gemini (may 21 - june 20)",
    "cancer (june 21 - july 22)",
    "leo (july 23 - august 22)",
    "virgo (august 23 - september 22)",
    "libra (september 23 - october 22)",
    "scorpio (october 23 - november 21)",
    "sagittarius (november 22 - december 21)",
    "capricorn (december 22 - january 19)",
    "aquarius (january 20 - february 18)",
    "pisces (february 19 - march 20)",
)

class SeasonMonthDictionary(list: Set<String>) : Dictionary(list) {
    class SeasonMonthDictionaryBuilder : Builder()

    enum class SeasonMonthTypes(private val seasonMonthSet: Set<String>) : Type {
        SEASONS(seasonsSet),
        MONTHS(monthsSet),
        ZODIAC_SIGNS(zodiacSignsSet),
        ALL(seasonsSet + monthsSet + zodiacSignsSet);

        override fun getSet(): Set<String> {
            return seasonMonthSet
        }
    }

    companion object {
        fun builder(block: SeasonMonthDictionaryBuilder.() -> Unit): SeasonMonthDictionary {
            val builder = SeasonMonthDictionaryBuilder()
            builder.block()
            return SeasonMonthDictionary(builder.build().list)
        }
    }

}
