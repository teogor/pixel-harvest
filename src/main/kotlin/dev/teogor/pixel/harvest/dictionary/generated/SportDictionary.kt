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
    "field hockey",
    "lacrosse",
    "netball",
    "kabaddi",
    "american football",
    "softball",
    "polo",
    "buzkashi",
    "korfball",
    "quidditch",
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
    "figure skating",
    "triathlon",
    "skateboarding",
    "hurdling",
    "bouldering",
    "parkour",
    "pole vaulting",
    "pole dancing",
    "rhythmic gymnastics",
)

private val racketSportsSet = setOf(
    "badminton",
    "table tennis",
    "squash",
    "pickleball",
    "tennis doubles",
    "racquetball",
    "padel",
    "beach tennis",
    "real tennis",
    "speedminton",
    "jai alai",
)

private val waterSportsSet = setOf(
    "kayaking",
    "rowing",
    "sailing",
    "diving",
    "windsurfing",
    "paddleboarding",
    "synchronized swimming",
    "water skiing",
    "scuba diving",
    "snorkeling",
    "water polo",
    "jet skiing",
    "canoeing",
    "rafting",
    "wakeboarding",
)

private val combatSportsSet = setOf(
    "wrestling",
    "judo",
    "taekwondo",
    "karate",
    "boxing",
    "muay thai",
    "jiu-jitsu",
    "sumo wrestling",
    "krav maga",
    "sambo",
    "kendo",
    "capoeira",
)

private val outdoorSportsSet = setOf(
    "archery",
    "climbing",
    "fishing",
    "hiking",
    "horseback riding",
    "mountain biking",
    "camping",
    "orienteering",
    "paragliding",
    "canopying",
    "trail running",
    "ultramarathon",
    "geocaching",
    "bungee jumping",
    "ice climbing",
)

private val winterSportsSet = setOf(
    "snowboarding",
    "ice hockey",
    "curling",
    "bobsleigh",
    "ski jumping",
    "speed skating",
    "biathlon",
    "figure skating",
    "skeleton",
    "cross-country skiing",
    "snowshoeing",
    "ice fishing",
    "snowmobiling",
    "snowkiting",
)

private val motorSportsSet = setOf(
    "formula 1",
    "motocross",
    "rally racing",
    "motorcycle racing",
    "go-karting",
    "drag racing",
    "superbike racing",
    "enduro racing",
    "off-road racing",
    "hill climbing",
    "monster truck racing",
    "powerboat racing",
    "snowmobile racing",
    "truck racing",
    "drifting",
)

private val targetSportsSet = setOf(
    "shooting",
    "darts",
    "billiards",
    "target archery",
    "skeet shooting",
    "golf darts",
    "bowling",
    "snooker",
    "lawn bowls",
    "axe throwing",
    "laser tag",
    "airsoft",
    "paintball",
    "horseshoes",
    "knife throwing",
)

private val adventureSportsSet = setOf(
    "skydiving",
    "paragliding",
    "rock climbing",
    "parkour",
    "canyoning",
    "base jumping",
    "kiteboarding",
    "caving",
    "ice climbing",
    "white-water rafting",
    "sandboarding",
    "slacklining",
    "canyoneering",
    "tree climbing",
    "ice swimming",
)

private val otherSportsSet = setOf(
    "chess",
    "poker",
    "bowling",
    "chess boxing",
    "ultimate frisbee",
    "canoe polo",
    "fencing",
    "golf",
    "swimming pool",
    "roller skating",
    "yoga",
    "cycling polo",
    "dog agility",
    "tug of war",
    "cheerleading",
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
