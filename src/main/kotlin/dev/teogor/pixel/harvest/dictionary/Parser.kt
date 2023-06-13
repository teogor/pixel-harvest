package dev.teogor.pixel.harvest.dictionary

import dev.teogor.pixel.harvest.dictionary.generated.ArtStyleDictionary
import dev.teogor.pixel.harvest.dictionary.generated.CameraAngleDictionary
import dev.teogor.pixel.harvest.dictionary.generated.ColorDictionary
import dev.teogor.pixel.harvest.dictionary.generated.ShapeDictionary
import dev.teogor.pixel.harvest.dictionary.generated.SportDictionary
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import java.io.File
import java.util.Locale
import java.util.SortedSet

fun parseTextFile(inputFile: File, outputFilePath: String, datasetName: String): SortedSet<String> {
    val name = datasetName.toCamelCase()
    val nameLower = name.replaceFirstChar { it.lowercase() }
    val className = "${name}Dictionary"
    val outputFile = File("${outputFilePath}/$className.kt")
    val sortedItems: SortedSet<String>

    val categories = mutableMapOf<String, MutableSet<String>>()
    val addedElements = mutableSetOf<String>()
    var currentCategory = ""
    var hasCategories = false // Track if any categories were found

    inputFile.forEachLine { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.startsWith("::category::")) {
            currentCategory = trimmedLine.removePrefix("::category::")
            hasCategories = true
        } else if (trimmedLine.isNotBlank()) {
            val element = trimmedLine.lowercase()
            if (!addedElements.contains(element)) {
                categories.getOrPut(currentCategory) { mutableSetOf() }.add(element)
                addedElements.add(element)
            }
        }
    }

    sortedItems = categories.values.flatten().toSortedSet()

    outputFile.bufferedWriter().use { writer ->
        writer.write("package dev.teogor.pixel.harvest.dictionary.generated\n\n")
        writer.write("import dev.teogor.pixel.harvest.dictionary.Dictionary\n\n")

        categories.forEach { (category, itemList) ->
            val categoryNameLower = category.split(" ")
                .joinToString("") {
                    it.replaceFirstChar {
                        if (it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else
                            it.toString()
                    }
                }
                .replaceFirstChar { it.lowercase() }
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
                val categoryName = category.uppercase().replace(" ", "_")
                val categoryNameLower = category.split(" ")
                    .joinToString("") {
                        it.replaceFirstChar {
                            if (it.isLowerCase())
                                it.titlecase(Locale.getDefault())
                            else
                                it.toString()
                        }
                    }
                    .replaceFirstChar { it.lowercase() }
                val variableName = "${categoryNameLower}Set"
                writer.write("        $categoryName(${variableName}),\n")
            }
        }
        val allListSet = if (hasCategories) {
            categories.keys.joinToString("Set + ") { category ->
                category.split(" ")
                    .joinToString("") {
                        it.replaceFirstChar {
                            if (it.isLowerCase())
                                it.titlecase(Locale.getDefault())
                            else
                                it.toString()
                        }
                    }
                    .replaceFirstChar { it.lowercase() }
            } + "Set"
        } else {
            "list"
        }
        writer.write("        ALL($allListSet);\n\n")
        writer.write("        override fun getSet(): Set<String> {\n")
        writer.write("            return ${nameLower}Set\n")
        writer.write("        }\n")
        writer.write("    }\n\n")
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
        categories.forEach { (category, itemList) ->
            writer.write("\n::category::${category.lowercase()}\n")
            itemList.forEach { item ->
                writer.write("$item\n")
            }
        }
    }

    return sortedItems
}

const val generatePrompt = true
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
            addType(ColorDictionary.ColorTypes.FUNDAMENTAL_COLORS)
            addType(ColorDictionary.ColorTypes.NEUTRAL_COLORS)
        } to 2,
        ArtStyleDictionary.builder {
            addType(ArtStyleDictionary.ArtStyleTypes.ALL)
        } to 1,
        ShapeDictionary.builder {
            addType(ShapeDictionary.ShapeTypes.ALL)
        } to 1,
        // SportDictionary.builder {
        //     addType(SportDictionary.SportTypes.ALL)
        // } to 1,
        // CameraAngleDictionary.builder {
        //     addType(CameraAngleDictionary.CameraAngleTypes.ALL)
        // } to 1
    )

    // Print the generated unique elements
    val prompt = "/imagine prompt:${uniqueElements.joinToString(", ")} --ar 16:9"
    println(prompt)
}

// Usage example
fun generateDictionary() {
    val directoryPath = "src/main/resources/dictionary/"
    val directory = File(directoryPath)
    val outputFilePath = "src/main/kotlin/dev/teogor/pixel/harvest/dictionary/generated"
    directory.listFilesWithExtensions(listOf("dict")) {
        parseTextFile(it, outputFilePath, it.nameWithoutExtension)
    }
}

fun String.toCamelCase(): String {
    val words = this.split("-").map { it.lowercase().capitalize() }
    return words.joinToString("").replace("-", "")
}