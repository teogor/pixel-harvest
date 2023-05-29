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

    outputFile.bufferedWriter().use { writer ->
        writer.write("package dev.teogor.pixel.harvest.dictionary.generated\n\n")
        writer.write("import dev.teogor.pixel.harvest.dictionary.Dictionary\n\n")

        val items = mutableListOf<String>()
        inputFile.forEachLine { line ->
            if (line.isNotBlank()) {
                items.add(line)
            }
        }
        sortedItems = items.toSortedSet()

        writer.write("private val list = setOf(\n")
        sortedItems.forEach { item ->
            writer.write("    \"$item\",\n")
        }
        writer.write(")\n\n")

        writer.write("class $className(list: Set<String>) : Dictionary(list) {\n")
        writer.write("    class ${className}Builder : Builder()\n\n")
        writer.write("    enum class ${className}Types(private val ${nameLower}Set: Set<String>) : Type {\n")
        writer.write("        ALL(list);\n\n")
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

    inputFile.bufferedWriter().use { writer ->
        sortedItems.forEach { item ->
            writer.write("${item.lowercase()}\n")
        }
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
    val colorDict = ColorDictionary.builder {
        addType(ColorDictionary.ColorTypes.FUNDAMENTAL)
        addType(ColorDictionary.ColorTypes.NEUTRAL)
    }
    colorDict
    val uniqueElements = generateUniqueElements(
        colorDict to 5,
        ArtStyleDictionary() to 2,
        ShapeDictionary() to 1,
        SportsDictionary() to 1,
        CameraAngleDictionary() to 1
    )

    // Print the generated unique elements
    val prompt = "/imagine prompt:${uniqueElements.joinToString(", ")} --ar 16:9"
    println(prompt)
}

// Usage example
fun generateDictionary() {

    val datasetName = "art-style-beta"
    val inputFilePath = "src/main/resources/dictionary/$datasetName.dict"
    val outputFilePath = "src/main/kotlin/dev/teogor/pixel/harvest/dictionary/generated"
    val items = parseTextFile(inputFilePath, outputFilePath, datasetName)
    // println("Generated file for ${items.size} Unique Entries")

}

fun String.toCamelCase(): String {
    val words = this.split("-").map { it.lowercase().capitalize() }
    return words.joinToString("").replace("-", "")
}