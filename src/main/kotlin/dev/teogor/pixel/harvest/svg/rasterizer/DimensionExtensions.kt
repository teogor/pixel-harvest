package dev.teogor.pixel.harvest.svg.rasterizer

/**
 * Calculates the height based on the given aspect ratio and width.
 *
 * @param aspectRatio The aspect ratio of the image.
 * @param width The width of the image.
 * @return The calculated height.
 */
fun calculateHeightFromAspectRatio(aspectRatio: Float, width: Float): Float {
    return (width / aspectRatio)
}

/**
 * Calculates the width based on the given aspect ratio and height.
 *
 * @param aspectRatio The aspect ratio of the image.
 * @param height The height of the image.
 * @return The calculated width.
 */
fun calculateWidthFromAspectRatio(aspectRatio: Float, height: Float): Float {
    return (height * aspectRatio)
}
