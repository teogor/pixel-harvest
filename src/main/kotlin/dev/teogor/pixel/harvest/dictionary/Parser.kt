package dev.teogor.pixel.harvest.dictionary

import dev.teogor.pixel.harvest.dictionary.generated.ArtStyleDictionary
import dev.teogor.pixel.harvest.dictionary.generated.ShapeDictionary
import java.io.File
import java.util.SortedSet

fun parseTextFile(inputFilePath: String, outputFilePath: String, datasetName: String): SortedSet<String> {
    val name = datasetName.toCamelCase()
    val nameLower = name.replaceFirstChar { it.lowercase() }
    val className = "${name}Dictionary"
    val inputFile = File(inputFilePath)
    val outputFile = File("${outputFilePath}/$className.kt")
    val sortedItems: SortedSet<String>

    val categories = mutableMapOf<String, MutableSet<String>>()
    var currentCategory = ""
    var hasCategories = false // Track if any categories were found

    inputFile.forEachLine { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.startsWith("::category::")) {
            currentCategory = trimmedLine.removePrefix("::category::")
            hasCategories = true
        } else if (trimmedLine.isNotBlank()) {
            categories.getOrPut(currentCategory) { mutableSetOf() }.add(trimmedLine)
        }
    }

    sortedItems = categories.values.flatten().toSortedSet()

    outputFile.bufferedWriter().use { writer ->
        writer.write("package dev.teogor.pixel.harvest.dictionary.generated\n\n")
        writer.write("import dev.teogor.pixel.harvest.dictionary.Dictionary\n\n")

        categories.forEach { (category, itemList) ->
            val categoryName = category.toCamelCase()
            val categoryNameLower = categoryName.replaceFirstChar { it.lowercase() }
            val variableName = if (hasCategories) {
                "${categoryNameLower}Set"
            } else {
                "list"
            }

            writer.write("private val $variableName = setOf(\n")
            itemList.forEach { item ->
                writer.write("    \"$item\",\n")
            }
            writer.write(")\n\n")
        }

        writer.write("class $className(list: Set<String>) : Dictionary(list) {\n")
        writer.write("    class ${className}Builder : Builder()\n\n")
        writer.write("    enum class ${name}Types(private val ${nameLower}Set: Set<String>) : Type {\n")
        if (hasCategories) {
            categories.forEach { (category, _) ->
                val categoryName = category.uppercase()
                writer.write("        $categoryName(${category}Set),\n")
            }
        }
        writer.write("        ALL(${if (hasCategories) categories.keys.joinToString("Set + ") + "Set" else "list"});\n\n")
        writer.write("        override fun getSet(): Set<String> {\n")
        writer.write("            return ${nameLower}Set\n")
        writer.write("        }\n")
        writer.write("    }\n")
        writer.write("    companion object {\n")
        writer.write("        fun builder(block: ${className}Builder.() -> Unit): $className {\n")
        writer.write("            val builder = ${className}Builder()\n")
        writer.write("            builder.block()\n")
        writer.write("            return $className(builder.build().list)\n")
        writer.write("        }\n")
        writer.write("    }\n\n")
        writer.write("}\n")
    }

    return sortedItems
}

const val generatePrompt = false
fun main() {
    if (generatePrompt) {
        repeat(5) {
            generatePrompt()
        }
    } else {
        generateDictionary()
    }
}

fun generateUniqueElements(vararg dictionaryCounts: Pair<Dictionary, Int>): List<String> {
    val randomElements = mutableListOf<String>()

    dictionaryCounts.forEach { (dictionary, count) ->
        val randomItems = dictionary.getRandomUniqueItems(count)
        randomElements.addAll(randomItems.map { it.lowercase() })
    }

    return randomElements
}

fun generatePrompt() {
    val uniqueElements = generateUniqueElements(
        ColorDictionary.builder {
            addType(ColorDictionary.ColorTypes.FUNDAMENTAL)
            addType(ColorDictionary.ColorTypes.NEUTRAL)
        } to 5,
        ArtStyleDictionary.builder {
            addType(ArtStyleDictionary.ArtStyleTypes.ALL)
        } to 2,
        ShapeDictionary.builder {
            addType(ShapeDictionary.ShapeTypes.ALL)
        } to 1,
        SportsDictionary() to 1,
        CameraAngleDictionary() to 1
    )

    // Print the generated unique elements
    val prompt = "/imagine prompt:${uniqueElements.joinToString(", ")} --ar 16:9"
    println(prompt)
}

// Usage example
fun generateDictionary() {

    val datasetName = "sport"
    val inputFilePath = "src/main/resources/dictionary/$datasetName.dict"
    val outputFilePath = "src/main/kotlin/dev/teogor/pixel/harvest/dictionary/generated"
    val items = parseTextFile(inputFilePath, outputFilePath, datasetName)
    // println("Generated file for ${items.size} Unique Entries")

}

fun String.toCamelCase(): String {
    val words = this.split("-").map { it.lowercase().capitalize() }
    return words.joinToString("").replace("-", "")
}