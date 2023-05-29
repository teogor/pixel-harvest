package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val teamSportsSet = setOf(
    "football",
    "basketball",
    "baseball",
    "soccer",
    "volleyball",
    "cricket",
    "rugby",
    "hockey",
    "handball",
    "water polo",
)

private val individualSportsSet = setOf(
    "tennis",
    "golf",
    "athletics",
    "swimming",
    "cycling",
    "boxing",
    "gymnastics",
    "skiing",
    "surfing",
    "martial arts",
)

private val racketSportsSet = setOf(
    "badminton",
    "table tennis",
    "squash",
    "pickleball",
)

private val waterSportsSet = setOf(
    "kayaking",
    "rowing",
    "sailing",
    "diving",
    "windsurfing",
    "paddleboarding",
    "synchronized swimming",
)

private val combatSportsSet = setOf(
    "wrestling",
    "judo",
    "taekwondo",
    "karate",
)

private val outdoorSportsSet = setOf(
    "archery",
    "climbing",
    "fishing",
    "hiking",
    "horseback riding",
)

private val winterSportsSet = setOf(
    "snowboarding",
    "ice hockey",
    "curling",
    "bobsleigh",
)

private val motorSportsSet = setOf(
    "formula 1",
    "motocross",
    "rally racing",
    "motorcycle racing",
)

private val targetSportsSet = setOf(
    "shooting",
    "darts",
    "billiards",
    "target archery",
)

private val adventureSportsSet = setOf(
    "skydiving",
    "paragliding",
    "rock climbing",
    "parkour",
)

private val otherSportsSet = setOf(
    "chess",
    "poker",
    "bowling",
    "chess boxing",
)

class SportDictionary(list: Set<String>) : Dictionary(list) {
    class SportDictionaryBuilder : Builder()

    enum class SportTypes(private val sportSet: Set<String>) : Type {
        TEAM_SPORTS(teamSportsSet),
        INDIVIDUAL_SPORTS(individualSportsSet),
        RACKET_SPORTS(racketSportsSet),
        WATER_SPORTS(waterSportsSet),
        COMBAT_SPORTS(combatSportsSet),
        OUTDOOR_SPORTS(outdoorSportsSet),
        WINTER_SPORTS(winterSportsSet),
        MOTOR_SPORTS(motorSportsSet),
        TARGET_SPORTS(targetSportsSet),
        ADVENTURE_SPORTS(adventureSportsSet),
        OTHER_SPORTS(otherSportsSet),
        ALL(teamSportsSet + individualSportsSet + racketSportsSet + waterSportsSet + combatSportsSet + outdoorSportsSet + winterSportsSet + motorSportsSet + targetSportsSet + adventureSportsSet + otherSportsSet);

        override fun getSet(): Set<String> {
            return sportSet
        }
    }

    companion object {
        fun builder(block: SportDictionaryBuilder.() -> Unit): SportDictionary {
            val builder = SportDictionaryBuilder()
            builder.block()
            return SportDictionary(builder.build().list)
        }
    }

}
