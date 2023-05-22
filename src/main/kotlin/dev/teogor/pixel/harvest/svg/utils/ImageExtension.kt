package dev.teogor.pixel.harvest.svg.utils

/**
 * Enum class representing the supported image extensions.
 *
 * @param formatName The format name of the image extension.
 */
enum class ImageExtension(val formatName: String) {
    /**
     * JPEG image extension.
     */
    JPEG("jpeg"),

    /**
     * JPG image extension.
     */
    JPG("jpg"),

    /**
     * PNG image extension.
     */
    PNG("png")
}
