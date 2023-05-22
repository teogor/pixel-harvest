package dev.teogor.pixel.harvest.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun String.createDirectoryIfNotExists(): File {
    val directory = Paths.get(this)
    return if (!Files.exists(directory)) {
        try {
            Files.createDirectories(directory)
            File(this)
        } catch (e: Exception) {
            e.printStackTrace()
            File("unknown")
        }
    } else {
        File(this)
    }
}

val String.extractFilename: String
    get() = this.extractText().replaceUnavailableSymbols().sanitizeFileName(8)

fun String.extractText(): String {
    val regexFirst = Regex("""^(.*?)--""")
    val matchResultFirst = regexFirst.find(this)
    val firstSubstring = matchResultFirst?.groupValues?.get(1)

    val finalSubstring = if (firstSubstring.isNullOrEmpty()) {
        val regexLast = Regex("""^(.*?)-[^-]*$""")
        val matchResultLast = regexLast.find(this)
        matchResultLast?.groupValues?.get(1) ?: this
    } else {
        firstSubstring
    }
        .replace("*", "")
        .replace(Regex("""<[^>]+>"""), "")
        .replace(",", "")
        .trim()

    return finalSubstring
}

fun getMaxLengthForFileName(): Int {
    val os = System.getProperty("os.name").lowercase()

    return when {
        os.contains("win") -> 260
        os.contains("mac") || os.contains("darwin") -> 255
        os.contains("nix") || os.contains("nux") || os.contains("bsd") -> 255
        else -> 255 // Default to a common maximum length
    }
}

fun String.replaceUnavailableSymbols(): String {
    val availableSymbols = Regex("""[^a-zA-Z0-9 ]""")
    return this.replace(availableSymbols, "")
}

fun String.sanitizeFileName(offset: Int = 0): String {
    val maxLength = 90 - offset // getMaxLengthForFileName()

    var sanitizedFileName = this.trim()

    if (sanitizedFileName.length > maxLength - offset) {
        sanitizedFileName = sanitizedFileName.substring(0, maxLength - offset)
    }

    return sanitizedFileName
}