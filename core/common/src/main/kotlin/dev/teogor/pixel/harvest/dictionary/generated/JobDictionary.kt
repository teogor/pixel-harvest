package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val informationTechnologySet = setOf(
    "software engineer",
    "data scientist",
    "web developer",
    "network administrator",
    "database administrator",
    "systems analyst",
    "cybersecurity analyst",
    "ai engineer",
    "it project manager",
    "ux designer",
)

private val businessAndFinanceSet = setOf(
    "accountant",
    "financial analyst",
    "investment banker",
    "human resources manager",
    "marketing manager",
    "business consultant",
    "operations manager",
    "economist",
    "risk analyst",
    "supply chain manager",
)

private val healthcareSet = setOf(
    "doctor",
    "nurse",
    "pharmacist",
    "physical therapist",
    "dentist",
    "medical technologist",
    "radiologist",
    "healthcare administrator",
    "occupational therapist",
    "speech-language pathologist",
)

private val educationSet = setOf(
    "teacher",
    "professor",
    "school counselor",
    "librarian",
    "education administrator",
    "education consultant",
    "special education teacher",
    "curriculum developer",
    "school principal",
    "college admissions counselor",
)

private val hospitalityAndTourismSet = setOf(
    "hotel manager",
    "tour guide",
    "chef",
    "event planner",
    "travel agent",
    "restaurant manager",
    "cruise ship director",
    "sommelier",
    "concierge",
    "hospitality coordinator",
)

private val artsAndEntertainmentSet = setOf(
    "actor",
    "musician",
    "graphic designer",
    "writer",
    "film director",
    "photographer",
    "fashion designer",
    "art director",
    "game designer",
    "animator",
)

private val engineeringSet = setOf(
    "civil engineer",
    "mechanical engineer",
    "electrical engineer",
    "chemical engineer",
    "aerospace engineer",
    "environmental engineer",
    "structural engineer",
    "biomedical engineer",
    "petroleum engineer",
    "robotics engineer",
)

private val salesAndMarketingSet = setOf(
    "sales representative",
    "marketing coordinator",
    "advertising manager",
    "public relations specialist",
    "brand manager",
    "digital marketing specialist",
    "market research analyst",
    "sales manager",
    "social media manager",
    "copywriter",
)

private val scienceAndResearchSet = setOf(
    "scientist",
    "research assistant",
    "lab technician",
    "environmental analyst",
    "biomedical scientist",
    "astrophysicist",
    "chemist",
    "microbiologist",
    "geologist",
)

private val governmentAndPublicServiceSet = setOf(
    "police officer",
    "firefighter",
    "politician",
    "social worker",
    "government administrator",
    "policy analyst",
    "intelligence analyst",
    "diplomat",
    "customs officer",
    "civil servant",
)

private val constructionAndTradesSet = setOf(
    "carpenter",
    "electrician",
    "plumber",
    "welder",
    "construction manager",
    "architect",
    "interior designer",
    "roofing contractor",
    "landscaper",
    "masonry worker",
)

class JobDictionary(list: Set<String>) : Dictionary(list) {
    class JobDictionaryBuilder : Builder()

    enum class JobTypes(private val jobSet: Set<String>) : Type {
        INFORMATION_TECHNOLOGY(informationTechnologySet),
        BUSINESS_AND_FINANCE(businessAndFinanceSet),
        HEALTHCARE(healthcareSet),
        EDUCATION(educationSet),
        HOSPITALITY_AND_TOURISM(hospitalityAndTourismSet),
        ARTS_AND_ENTERTAINMENT(artsAndEntertainmentSet),
        ENGINEERING(engineeringSet),
        SALES_AND_MARKETING(salesAndMarketingSet),
        SCIENCE_AND_RESEARCH(scienceAndResearchSet),
        GOVERNMENT_AND_PUBLIC_SERVICE(governmentAndPublicServiceSet),
        CONSTRUCTION_AND_TRADES(constructionAndTradesSet),
        ALL(informationTechnologySet + businessAndFinanceSet + healthcareSet + educationSet + hospitalityAndTourismSet + artsAndEntertainmentSet + engineeringSet + salesAndMarketingSet + scienceAndResearchSet + governmentAndPublicServiceSet + constructionAndTradesSet);

        override fun getSet(): Set<String> {
            return jobSet
        }
    }

    companion object {
        fun builder(block: JobDictionaryBuilder.() -> Unit): JobDictionary {
            val builder = JobDictionaryBuilder()
            builder.block()
            return JobDictionary(builder.build().list)
        }
    }

}
