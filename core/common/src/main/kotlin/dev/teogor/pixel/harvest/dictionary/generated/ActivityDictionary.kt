package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val businessActivitiesSet = setOf(
    "meeting with clients",
    "conference call",
    "networking event",
    "business lunch",
    "team building activity",
    "product presentation",
    "market research",
    "business negotiation",
    "sales pitch",
    "business planning",
)

private val nightlifeActivitiesSet = setOf(
    "clubbing",
    "bar hopping",
    "live music concert",
    "dancing",
    "karaoke night",
    "pub quiz",
    "comedy show",
    "late-night dining",
    "casino night",
    "nighttime city tour",
)

private val outdoorActivitiesSet = setOf(
    "hiking",
    "cycling",
    "picnic in the park",
    "beach volleyball",
    "kayaking",
    "sightseeing tour",
    "nature walk",
    "camping",
    "gardening",
    "outdoor yoga",
)

private val sportsActivitiesSet = setOf(
    "football match",
    "basketball game",
    "tennis match",
    "golfing",
    "swimming",
    "running",
    "cycling race",
    "skiing",
    "surfing",
    "martial arts class",
)

private val creativeActivitiesSet = setOf(
    "painting",
    "drawing",
    "photography",
    "pottery making",
    "writing",
    "sculpting",
    "crafting",
    "cooking class",
    "dance class",
    "acting workshop",
)

private val educationalActivitiesSet = setOf(
    "workshop",
    "seminar",
    "training session",
    "webinar",
    "language class",
    "online course",
    "book club meeting",
    "science exhibition",
    "historical tour",
    "art museum visit",
)

private val recreationalActivitiesSet = setOf(
    "movie night",
    "bowling",
    "billiards",
    "video gaming",
    "escape room challenge",
    "spa day",
    "amusement park visit",
    "go-kart racing",
    "mini-golfing",
    "virtual reality experience",
)

private val wellnessActivitiesSet = setOf(
    "yoga",
    "meditation",
    "fitness class",
    "massage therapy",
    "hiking retreat",
    "aromatherapy session",
    "healthy cooking workshop",
    "wellness retreat",
    "pilates",
    "tai chi",
)

private val culturalActivitiesSet = setOf(
    "theater play",
    "opera performance",
    "art gallery visit",
    "music festival",
    "film screening",
    "cultural festival",
    "historical landmark tour",
    "ethnic cuisine cooking class",
    "traditional dance performance",
    "local folklore event",
)

class ActivityDictionary(list: Set<String>) : Dictionary(list) {
    class ActivityDictionaryBuilder : Builder()

    enum class ActivityTypes(private val activitySet: Set<String>) : Type {
        BUSINESS_ACTIVITIES(businessActivitiesSet),
        NIGHTLIFE_ACTIVITIES(nightlifeActivitiesSet),
        OUTDOOR_ACTIVITIES(outdoorActivitiesSet),
        SPORTS_ACTIVITIES(sportsActivitiesSet),
        CREATIVE_ACTIVITIES(creativeActivitiesSet),
        EDUCATIONAL_ACTIVITIES(educationalActivitiesSet),
        RECREATIONAL_ACTIVITIES(recreationalActivitiesSet),
        WELLNESS_ACTIVITIES(wellnessActivitiesSet),
        CULTURAL_ACTIVITIES(culturalActivitiesSet),
        ALL(businessActivitiesSet + nightlifeActivitiesSet + outdoorActivitiesSet + sportsActivitiesSet + creativeActivitiesSet + educationalActivitiesSet + recreationalActivitiesSet + wellnessActivitiesSet + culturalActivitiesSet);

        override fun getSet(): Set<String> {
            return activitySet
        }
    }

    companion object {
        fun builder(block: ActivityDictionaryBuilder.() -> Unit): ActivityDictionary {
            val builder = ActivityDictionaryBuilder()
            builder.block()
            return ActivityDictionary(builder.build().list)
        }
    }

}
