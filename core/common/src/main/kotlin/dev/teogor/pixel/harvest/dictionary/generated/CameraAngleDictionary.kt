package dev.teogor.pixel.harvest.dictionary.generated

import dev.teogor.pixel.harvest.dictionary.Dictionary

private val basicAnglesSet = setOf(
    "bird's-eye view",
    "eye level",
    "high angle",
    "low angle",
    "dutch angle",
    "over-the-shoulder shot",
    "point of view (pov) shot",
    "wide shot",
)

private val advancedAnglesSet = setOf(
    "aerial shot",
    "canted angle",
    "crane shot",
    "extreme close-up",
    "fish-eye shot",
    "helicopter shot",
    "long shot",
    "medium close-up",
    "medium shot",
    "panning shot",
    "tilt shot",
    "tracking shot",
    "zoom shot",
)

private val creativeAnglesSet = setOf(
    "dolly zoom",
    "freeze frame",
    "high-key lighting",
    "low-key lighting",
    "oblique angle",
    "silhouette shot",
    "split screen",
    "top-down shot",
    "undershot",
    "whip pan",
)

class CameraAngleDictionary(list: Set<String>) : Dictionary(list) {
    class CameraAngleDictionaryBuilder : Builder()

    enum class CameraAngleTypes(private val cameraAngleSet: Set<String>) : Type {
        BASIC_ANGLES(basicAnglesSet),
        ADVANCED_ANGLES(advancedAnglesSet),
        CREATIVE_ANGLES(creativeAnglesSet),
        ALL(basicAnglesSet + advancedAnglesSet + creativeAnglesSet);

        override fun getSet(): Set<String> {
            return cameraAngleSet
        }
    }

    companion object {
        fun builder(block: CameraAngleDictionaryBuilder.() -> Unit): CameraAngleDictionary {
            val builder = CameraAngleDictionaryBuilder()
            builder.block()
            return CameraAngleDictionary(builder.build().list)
        }
    }

}
