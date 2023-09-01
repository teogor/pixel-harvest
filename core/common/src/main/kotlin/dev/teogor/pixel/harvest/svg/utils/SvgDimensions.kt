package dev.teogor.pixel.harvest.svg.utils

/**
 * Constructs a new SvgDimensions object with the specified width, height, and aspect ratio.
 *
 * @param width       The width of the SVG image.
 * @param height      The height of the SVG image.
 * @param aspectRatio The aspect ratio of the SVG image (width / height).
 */
data class SvgDimensions(
    val width: Float,
    val height: Float,
    val aspectRatio: Float,
)