package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val ageSet = setOf(
    "infant",
    "child",
    "preteen",
    "teenager",
    "young adult",
    "adult",
    "middle-aged adult",
    "senior citizen",
)

private val genderSet = setOf(
    "male",
    "female",
    "non-binary",
    "transgender",
    "genderqueer",
    "genderfluid",
    "other",
)

private val educationLevelSet = setOf(
    "no formal education",
    "preschool",
    "primary school",
    "secondary school",
    "high school diploma",
    "vocational training",
    "associate's degree",
    "bachelor's degree",
    "master's degree",
    "doctorate degree",
)

private val occupationSet = setOf(
    "unemployed",
    "student",
    "entry-level worker",
    "mid-level professional",
    "senior-level professional",
    "entrepreneur",
    "freelancer",
    "homemaker",
    "retired",
)

private val maritalStatusSet = setOf(
    "single",
    "married",
    "divorced",
    "separated",
    "widowed",
)

private val parentalStatusSet = setOf(
    "no children",
    "expecting a child",
    "parent of infants",
    "parent of toddlers",
    "parent of school-age children",
    "parent of teenagers",
    "empty nester",
)

private val ethnicitySet = setOf(
    "caucasian",
    "african american",
    "hispanic/latinx",
    "asian",
    "native american",
    "middle eastern",
    "multiracial",
)

private val religionSet = setOf(
    "christianity",
    "islam",
    "judaism",
    "hinduism",
    "buddhism",
    "sikhism",
)

private val socioeconomicStatusSet = setOf(
    "low income",
    "middle income",
    "high income",
)

private val disabilityStatusSet = setOf(
    "physical disability",
    "intellectual disability",
    "sensory disability",
    "mental health condition",
    "chronic illness",
)

private val lgbtqIdentitySet = setOf(
    "lesbian",
    "gay",
    "bisexual",
    "queer",
    "pansexual",
    "asexual",
)

private val veteranStatusSet = setOf(
    "veteran",
    "non-veteran",
)

class PeopleDictionary(list: Set<String>) : Dictionary(list) {
    class PeopleDictionaryBuilder : Builder()

    enum class PeopleTypes(private val peopleSet: Set<String>) : Type {
        AGE(ageSet),
        GENDER(genderSet),
        EDUCATION_LEVEL(educationLevelSet),
        OCCUPATION(occupationSet),
        MARITAL_STATUS(maritalStatusSet),
        PARENTAL_STATUS(parentalStatusSet),
        ETHNICITY(ethnicitySet),
        RELIGION(religionSet),
        SOCIOECONOMIC_STATUS(socioeconomicStatusSet),
        DISABILITY_STATUS(disabilityStatusSet),
        LGBTQ_IDENTITY(lgbtqIdentitySet),
        VETERAN_STATUS(veteranStatusSet),
        ALL(ageSet + genderSet + educationLevelSet + occupationSet + maritalStatusSet + parentalStatusSet + ethnicitySet + religionSet + socioeconomicStatusSet + disabilityStatusSet + lgbtqIdentitySet + veteranStatusSet);

        override fun getSet(): Set<String> {
            return peopleSet
        }
    }

    companion object {
        fun builder(block: PeopleDictionaryBuilder.() -> Unit): PeopleDictionary {
            val builder = PeopleDictionaryBuilder()
            builder.block()
            return PeopleDictionary(builder.build().list)
        }
    }

}
