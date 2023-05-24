package dev.teogor.pixel.harvest.beta

class ColorPalette {
    // todo classification like level 1, 2, 3
    private val standardColors = listOf(
        "Red",
        "Blue",
        "Green",
        "Yellow",
        "Orange",
        "Purple",
        "Pink",
        "Brown",
        "Black",
        "Gray",
        "White",
        "Cyan",
        "Beige",
        "Teal",
    )

    private val additionalColors = listOf(
        "Aquamarine",
        "Chartreuse",
        "Crimson",
        "Magenta",
        "Violet",
        "Lavender",
        "Maroon",
        "Navy",
        "Olive",
        "DarkCyan",
        "DarkMagenta",
        "Silver",
        "Gold",
        "Indigo",
        "Turquoise",
        "DeepPink",
        "Gold",
        "LimeGreen",
        "MidnightBlue",
        "OliveDrab",
        "PaleVioletRed",
        "SandyBrown",
        "SkyBlue",
        "SpringGreen",
        "Tomato"
    )

    fun getRandomColor(
        includeAdditionalColors: Boolean = false,
    ): String {
        val allColors = standardColors + if (includeAdditionalColors) additionalColors else emptyList()
        return allColors.random()
    }
}

class ColorPairGenerator {
    private val colorPalette: ColorPalette = ColorPalette()

    fun generateRandomColorPairs(
        pairCount: Int,
        format: String,
        allowReversed: Boolean = false,
        includeAdditionalColors: Boolean = false,
    ): List<String> {
        val pairs = mutableSetOf<String>()

        while (pairs.size < pairCount) {
            val color1 = colorPalette.getRandomColor(includeAdditionalColors)
            val color2 = colorPalette.getRandomColor(includeAdditionalColors)

            // Ensure unique colors in the pair
            val pairKey = "$color1 $color2"
            val reversedPairKey = if (allowReversed) "$color1 $color2" else "$color2 $color1"

            if (color1 != color2 && !(pairs.contains(pairKey) || pairs.contains(reversedPairKey))) {
                val formattedOutput = format
                    .replace("\$color1", color1)
                    .replace("\$c1", color1)
                    .replace("\$color2", color2)
                    .replace("\$c2", color2)

                pairs.add(formattedOutput)
            }
        }

        return pairs.toList()
    }
}

fun main() {
    val pairCount = 10 // Number of random color pairs to generate
    val format = "\$color1 to \$color2" // Custom formatting string
    val prompt = "Abstract Fluid Multicolors Background from \$colors"
    val imaginePrompt = "/imagine prompt:"
    val colorPairGenerator = ColorPairGenerator()
    val pairs = colorPairGenerator.generateRandomColorPairs(
        pairCount = pairCount,
        format = format,
        allowReversed = false,
        includeAdditionalColors = false,
    )
    val midjourneySyntax = pairs.joinToString(separator = ",", prefix = "{", postfix = "}") {
        "$it"
    }
    val endPrompt = "$imaginePrompt${prompt.replace("\$colors", midjourneySyntax)}"
    println(endPrompt)
}
