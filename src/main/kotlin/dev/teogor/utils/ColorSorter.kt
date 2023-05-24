package dev.teogor.utils

import dev.kord.common.Color
import java.io.File

private const val path = "src/main/kotlin/dev/teogor/pixel/harvest/utils/Colors.kt"

fun main() {
    val inputFile = File(path) // Replace with the actual path to your input file
    val outputFile = File(path) // Replace with the desired path to the output file

    val colorRegex = Regex("val (\\w+) = Color\\((\\d+), (\\d+), (\\d+)\\)")

    val colorMap = mutableMapOf<String, Color>()

    inputFile.forEachLine { line ->
        val matchResult = colorRegex.find(line)
        if (matchResult != null) {
            val (name, red, green, blue) = matchResult.destructured
            colorMap[name] = Color(red.toInt(), green.toInt(), blue.toInt())
        }
    }

    val sortedColors = colorMap.entries.sortedBy { it.key }

    val outputLines = mutableListOf<String>()

    val sortedColorLines = sortedColors.map { (name, color) ->
        "val $name = Color(${color.red}, ${color.green}, ${color.blue})"
    }

    for ((name, color) in sortedColors) {
        println("val $name = Color(${color.red}, ${color.green}, ${color.blue})")
    }

    inputFile.forEachLine { line ->
        if (colorRegex.matches(line)) {
            // Skip the color definition line
            return@forEachLine
        }
        outputLines.add(line)
        if (line.trim() == "}") {
            // Add the sorted color lines after the closing bracket
            outputLines.addAll(sortedColorLines)
        }
    }

    outputFile.bufferedWriter().use { writer ->
        outputLines.forEach { line ->
            writer.write(line)
            writer.newLine()
        }
    }

    println("Output file has been written successfully.")
}