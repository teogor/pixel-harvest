package dev.teogor.pixel.harvest.svg.rasterizer

import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import java.io.File

/**
 * Represents a file for SVG conversion.
 *
 * @property inputFile The input SVG file to be converted.
 * @property outputFolder The output folder where the converted image will be saved.
 * @property scaleFactor The scale factor to be applied during conversion.
 * @property scalingOptions The options for scaling the SVG file (default value is an instance of [ScalingOptions]).
 * @property extension The image extension to be used for the output file (default value is [ImageExtension.JPEG]).
 * @property fileNamePattern A function to generate the output file name based on the input file name
 *                           (default value is an identity function).
 */
data class SvgFile(
    val inputFile: File,
    val outputFolder: File,
    val scaleFactor: Double,
    val scalingOptions: ScalingOptions = ScalingOptions(),
    val extension: ImageExtension = ImageExtension.JPEG,
    val fileNamePattern: (String) -> String = { inputFileName ->
        inputFileName
    }
) {
    /**
     * Returns the output file name based on the input file name and extension.
     *
     * @return The output file name.
     */
    val outputFileName: String
        get() = "${fileNamePattern(inputFile.nameWithoutExtension)}.${extension.formatName}"

    /**
     * Returns the output file where the converted image will be saved.
     * The output folder will be created if it doesn't exist.
     *
     * @return The output file.
     */
    val outputFile: File
        get() {
            outputFolder.mkdirs()
            return File(outputFolder, outputFileName)
        }
}
