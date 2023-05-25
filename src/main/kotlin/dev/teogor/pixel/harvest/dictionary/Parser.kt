package dev.teogor.pixel.harvest.dictionary

import dev.teogor.pixel.harvest.dictionary.generated.ArtStyleDictionary
import dev.teogor.pixel.harvest.dictionary.generated.ShapeDictionary
import java.io.File
import java.util.SortedSet

fun parseTextFile(inputFilePath: String, outputFilePath: String, datasetName: String): SortedSet<String> {
    val className = "${datasetName.toCamelCase()}Dictionary"
    val inputFile = File(inputFilePath)
    val outputFile = File("${outputFilePath}/$className.kt")
    val sortedItems: SortedSet<String>

    outputFile.bufferedWriter().use { writer ->
        writer.write("package dev.teogor.pixel.harvest.dictionary.generated\n\n")
        writer.write("import dev.teogor.pixel.harvest.dictionary.Dictionary\n\n")
        writer.write("class $className : Dictionary() {\n")
        writer.write("    override val list: Set<String> = setOf(\n")

        val items = mutableListOf<String>()
        inputFile.forEachLine { line ->
            if (line.isNotBlank()) {
                items.add(line)
            }
        }
        sortedItems = items.toSortedSet()
        sortedItems.forEach { item ->
            writer.write("        \"$item\",\n")
        }

        writer.write("    )\n")
        writer.write("}\n")
    }


    inputFile.bufferedWriter().use { writer ->
        sortedItems.forEach { item ->
            writer.write("${item.lowercase()}\n")
        }
    }

    return sortedItems
}

const val generatePrompt = true
fun main() {
    if (generatePrompt) {
        generatePrompt()
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

    val datasetName = "shape"
    val inputFilePath = "src/main/resources/dictionary/$datasetName.dict"
    val outputFilePath = "src/main/kotlin/dev/teogor/pixel/harvest/dictionary/generated"
    val items = parseTextFile(inputFilePath, outputFilePath, datasetName)
    // println("Generated file for ${items.size} Unique Entries")

}

fun String.toCamelCase(): String {
    val words = this.split("-").map { it.lowercase().capitalize() }
    return words.joinToString("").replace("-", "")
}