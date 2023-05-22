package dev.teogor.pixel.harvest.svg.rasterizer

/**
 * Options for scaling an SVG file during conversion.
 *
 * @property scale The scaling factor to be applied uniformly to both width and height (default value is `null`).
 * @property targetWidth The target width of the scaled SVG file (default value is `null`).
 * @property targetHeight The target height of the scaled SVG file (default value is `null`).
 */
data class ScalingOptions(
    val scale: Float? = null,
    val targetWidth: Float? = null,
    val targetHeight: Float? = null
)
