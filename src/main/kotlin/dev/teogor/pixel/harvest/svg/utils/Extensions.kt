package dev.teogor.pixel.harvest.svg.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Generates a file name template by replacing placeholders with the provided values.
 *
 * @param fileName   The value to replace the "$fileName" placeholder.
 * @param fileCount  The value to replace the "$fileCount" placeholder.
 * @param timestamp  The value to replace the "$timestamp" placeholder.
 * @return The generated file name template.
 */
fun String.generateFileNameTemplate(
    fileName: String = "",
    fileCount: String = "",
    timestamp: String = "",
): String {
    return this
        .safeReplace("\$fileName", fileName)
        .safeReplace("\$fileCount", fileCount)
        .safeReplace("\$timestamp", timestamp)
}

/**
 * Replaces a placeholder with a replacement value in a string.
 *
 * @param placeholder The placeholder to replace.
 * @param replacement The replacement value.
 * @return The string with the placeholder replaced.
 * @throws IllegalArgumentException if the replacement value is empty.
 */
fun String.safeReplace(placeholder: String, replacement: String): String {
    return if (contains(placeholder)) {
        require(replacement.isNotEmpty()) { "Replacement value for $placeholder is empty." }
        replace(placeholder, replacement)
    } else {
        this
    }
}

/**
 * Formats a duration in milliseconds to a human-readable string.
 *
 * @return The formatted duration string.
 */
fun Long.formatDuration(): String {
    if (this < 1000) {
        return "$this ms"
    }

    val seconds = this / 1000
    if (seconds < 60) {
        return "$seconds sec"
    }

    val minutes = seconds / 60
    if (minutes < 60) {
        val remainingSeconds = seconds % 60
        return "$minutes min $remainingSeconds sec"
    }

    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return "$hours hr $remainingMinutes min"
}

/**
 * Retrieves files in a directory with specified extensions and performs an action on each file.
 *
 * @param extensions The list of file extensions to match.
 * @param action     The action to perform on each matching file.
 */
fun File.listFilesWithExtensions(
    extensions: List<String>,
    action: (File) -> Unit,
) {
    this.listFiles { file ->
        file.isFile && extensions.any { extension ->
            file.extension.equals(extension, ignoreCase = true)
        }
    }?.forEach {
        if (it.exists()) {
            action(it)
        }
    }
}



/**
 * Generates a random number between 1000 and 9999 (inclusive).
 *
 * @return The generated random number.
 */
fun generateRandomNumber(): Int {
    return (1_000..9_999).random()
}

/**
 * Formats the current date and time as a string in the "yyyyMMdd_HHmmss" pattern.
 *
 * @return The formatted date and time string.
 */
fun getFormattedDate(): String {
    return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}

/**
 * Formats the current date and time as a string in the "yyyy-MM-dd HH:mm:ss" pattern.
 *
 * @return The formatted date and time string.
 */
fun getFormattedDate2(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}

/**
 * Creates a directory if it does not exist.
 *
 * @return The created directory.
 */
fun String.createDirectoryIfNotExists(): File {
    val directory = File(this)
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}