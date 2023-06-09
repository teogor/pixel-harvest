package dev.teogor.pixel.harvest.utils

import dev.kord.core.entity.interaction.OptionValue

fun Any?.asBooleanOrDefault(defaultValue: Boolean = false): Boolean {
    return try {
        this?.toString()?.toBoolean() ?: defaultValue
    } catch (e: IllegalArgumentException) {
        defaultValue
    }
}

fun Any?.asFloatOrDefault(defaultValue: Float = 0.0f): Float {
    return try {
        this?.toString()?.toFloat() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun OptionValue<*>?.asStringDefault(defaultValue: String = ""): String {
    return this?.value?.toString() ?: defaultValue
}

@Deprecated(message = "Not getting right", replaceWith = ReplaceWith(expression = "asStringDefault"))
fun Any?.asStringOrDefault(defaultValue: String = ""): String {
    return this?.toString() ?: defaultValue
}

fun Any?.asIntOrDefault(defaultValue: Int? = null): Int? {
    return try {
        this?.toString()?.toInt() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun Any?.asLongOrDefault(defaultValue: Long? = null): Long? {
    return try {
        this?.toString()?.toLong() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}