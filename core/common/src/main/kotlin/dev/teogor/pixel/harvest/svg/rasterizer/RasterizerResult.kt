package dev.teogor.pixel.harvest.svg.rasterizer

import java.io.File

/**
 * Represents the result of an SVG rasterization operation.
 */
sealed class RasterizerResult {
    /**
     * Represents the file information of the rasterized output.
     *
     * @property inputFile The input SVG file.
     * @property outputFile The output rasterized file.
     */
    data class FileInfo(
        val inputFile: File,
        val outputFile: File
    )

    /**
     * Represents a successful rasterization result.
     *
     * @property width The width of the rasterized image.
     * @property height The height of the rasterized image.
     * @property aspectRatio The aspect ratio of the rasterized image.
     * @property scaleFactor The scaling factor applied to the SVG image.
     * @property totalTime The total time taken for the rasterization process.
     * @property loadTime The time taken to load the SVG document.
     * @property scaleTime The time taken to scale the SVG document.
     * @property saveTime The time taken to save the rasterized image.
     * @property fileInfo The file information of the rasterized output.
     */
    data class Success(
        val width: Float,
        val height: Float,
        val aspectRatio: Float,
        val scaleFactor: Double,
        val totalTime: Long,
        val loadTime: Long,
        val scaleTime: Long,
        val saveTime: Long,
        val fileInfo: FileInfo,
    ) : RasterizerResult()

    /**
     * Represents an error that occurred during the rasterization process.
     *
     * @property errorMessage The error message describing the failure.
     * @property fileInfo The file information at the time of the error.
     */
    data class Error(
        val errorMessage: String,
        val fileInfo: FileInfo,
    ) : RasterizerResult()
}

/**
 * Process the rasterization result by invoking the appropriate handler based on the result type.
 *
 * @param onSuccess The handler function to be called when the result is a success.
 * @param onError The handler function to be called when the result is an error.
 */
fun RasterizerResult.processResult(
    onSuccess: (RasterizerResult.Success) -> Unit,
    onError: (RasterizerResult.Error) -> Unit,
) {
    when (this) {
        is RasterizerResult.Success -> {
            onSuccess(this)
        }

        is RasterizerResult.Error -> {
            onError(this)
        }
    }
}
