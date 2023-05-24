package dev.teogor.pixel.harvest.svg

import dev.teogor.pixel.harvest.svg.generator.SvgGenerator
import dev.teogor.pixel.harvest.svg.rasterizer.SvgFile
import dev.teogor.pixel.harvest.svg.rasterizer.SvgRasterizer
import dev.teogor.pixel.harvest.svg.rasterizer.processResult
import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import dev.teogor.pixel.harvest.svg.utils.formatDuration
import dev.teogor.pixel.harvest.svg.utils.generateFileNameTemplate
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * The SvgConverter class provides functionality to convert SVG files and images.
 *
 * Constructs an instance of SvgConverter with the specified parameters.
 *
 * @param inputFolder      The input folder containing the SVG files.
 * @param outputFolder     The output folder for the converted images.
 * @param generator        The SvgGenerator instance for converting images to SVG.
 * @param rasterizer       The SvgRasterizer instance for converting SVG to images.
 * @param useSvgGenerator  Flag indicating whether to use the SvgGenerator.
 * @param useSvgRasterizer Flag indicating whether to use the SvgRasterizer.
 * @param batchNumber      The batch number for the converted images.
 */
class SvgConverter private constructor(
    private val inputFolder: File,
    private val outputFolder: File,
    private val generator: SvgGenerator,
    private val rasterizer: SvgRasterizer,
    var useSvgGenerator: Boolean,
    var useSvgRasterizer: Boolean,
    private val progressListener: ProgressListener,
    private val batchNumber: Int,
) {

    private val generatorInputDirectory: String = if (useSvgGenerator) {
        inputFolder.path
    } else {
        ""
    }
    private val generatorOutputDirectory: String = if (useSvgGenerator) {
        "${outputFolder.path}/batch-${batchNumber.toString().padStart(6, '0')}"
    } else {
        ""
    }
    private val rasterizerInputDirectory: String = if (useSvgRasterizer) {
        if (useSvgGenerator) {
            "$generatorOutputDirectory/svg"
        } else {
            inputFolder.path
        }
    } else {
        ""
    }
    private val rasterizerOutputDirectory: String = if (useSvgRasterizer) {
        if (useSvgGenerator) {
            "$generatorOutputDirectory/image-export"
        } else {
            outputFolder.path
        }
    } else {
        ""
    }

    init {
        initializeConversion()
    }


    /**
     * Initializes the conversion process based on the selected options.
     */
    private fun initializeConversion() {
        if (useSvgGenerator && useSvgRasterizer) {
            convertToSvg()
            convertToImage()
        } else if (useSvgGenerator) {
            convertToSvg()
        } else if (useSvgRasterizer) {
            convertToImage()
        }
    }

    /**
     * Converts images to SVG using the SvgGenerator.
     */
    private fun convertToSvg() {
        generator.inputDirectory = generatorInputDirectory
        generator.outputDirectory = generatorOutputDirectory
        generator.convertImages()
    }

    /**
     * Converts SVG files to images using the SvgRasterizer.
     */
    private fun convertToImage() {
        val directoryInput = File(rasterizerInputDirectory)
        val directoryExport = File(rasterizerOutputDirectory)
        val saveExtension = ImageExtension.JPEG

        var totalProcessingTime = 0L
        var totalLoadTime = 0L
        var totalScaleTime = 0L
        var totalSaveTime = 0L
        var processedCount = 0

        val progressData = ProgressData(
            totalDownloadedImages = 0,
            currentScaledImages = 0,
            currentSvgConverted = 0
        )

        runBlocking {
            progressListener.onProgress(progressData.copy(
                totalDownloadedImages = directoryInput.countFiles("")
            ))
        }
        directoryInput.listFilesWithExtensions(listOf("svg")) { svgFile ->
            val conversionResult = rasterizer.convert(
                SvgFile(
                    inputFile = svgFile,
                    outputFolder = directoryExport,
                    scaleFactor = 6.0,
                    extension = saveExtension,
                    fileNamePattern = { fileName ->
                        val lastIndex = fileName.lastIndexOf("_")
                        if (lastIndex != -1) {
                            val fileCount = (processedCount + 1).toString().padStart(3, '0')
                            val fileNameStr = fileName.substring(0, lastIndex).replace("teogor_", "")

                            val filename = "\$fileName_converted-\$fileCount"
                            val tFilename = filename.generateFileNameTemplate(
                                fileName = fileNameStr,
                                fileCount = fileCount,
                            )
                            tFilename
                        } else {
                            fileName
                        }
                    }
                )
            )
            conversionResult.processResult(
                onSuccess = {
                    with(it) {
                        println("SVG scaled and saved successfully as ${fileInfo.outputFile.name}!")
                        println("Processing Time: ${totalTime.formatDuration()}")
                        println("Loading Time:    ${loadTime.formatDuration()}")
                        println("Scaling Time:    ${scaleTime.formatDuration()}")
                        println("Saving Time:     ${saveTime.formatDuration()}")
                        println("")
                        totalProcessingTime += totalTime
                        totalLoadTime += loadTime
                        totalScaleTime += scaleTime
                        totalSaveTime += saveTime
                        processedCount++
                    }
                },
                onError = {
                    System.err.println(it.errorMessage)
                },
            )
        }

        if (processedCount == 0) {
            return
        }

        val averageProcessingTime = totalProcessingTime / processedCount
        val averageLoadTime = totalLoadTime / processedCount
        val averageScaleTime = totalScaleTime / processedCount
        val averageSaveTime = totalSaveTime / processedCount

        println("Processed Images: $processedCount")
        println("Total Processing Time: ${totalProcessingTime.formatDuration()}")
        println("Total Loading Time:    ${totalLoadTime.formatDuration()}")
        println("Total Scaling Time:    ${totalScaleTime.formatDuration()}")
        println("Total Saving Time:     ${totalSaveTime.formatDuration()}")
        println("Average Processing Time: ${averageProcessingTime.formatDuration()}")
        println("Average Loading Time:    ${averageLoadTime.formatDuration()}")
        println("Average Scaling Time:    ${averageScaleTime.formatDuration()}")
        println("Average Saving Time:     ${averageSaveTime.formatDuration()}")

    }

    /**
     * Builder class for creating an instance of SvgConverter.
     *
     * Constructs a Builder instance with the specified input and output folders.
     *
     * @param inputFolder  The input folder containing the SVG files.
     * @param outputFolder The output folder for the converted images.
     */
    class Builder(private val inputFolder: File, private val outputFolder: File) {
        private var useSvgGenerator: Boolean = false
        private var useSvgRasterizer: Boolean = false
        private var batchNumber: Int = 0
        private var progressListener: ProgressListener = object : ProgressListener() {
            override suspend fun onProgress(progressData: ProgressData) {
            }
        }

        /**
         * Sets whether to use the SvgGenerator.
         *
         * @param enabled Flag indicating whether to use the SvgGenerator.
         * @return The Builder instance.
         */
        fun withSvgGenerator(enabled: Boolean) = apply {
            this.useSvgGenerator = enabled
        }

        /**
         * Sets whether to use the SvgRasterizer.
         *
         * @param enabled Flag indicating whether to use the SvgRasterizer.
         * @return The Builder instance.
         */
        fun withSvgRasterizer(enabled: Boolean) = apply {
            this.useSvgRasterizer = enabled
        }

        /**
         * Sets the batch number for the converted images.
         *
         * @param number The batch number.
         * @return The Builder instance.
         */
        fun withBatchNumber(number: Int) = apply {
            this.batchNumber = number
        }

        /**
         * Sets a custom progress listener for tracking the progress of image conversion.
         *
         * @param listener The progress listener to be set.
         * @return The Builder instance.
         */
        fun withProgressListener(listener: ProgressListener) = apply {
            this.progressListener = listener
        }

        /**
         * Builds and returns an instance of SvgConverter with the specified parameters.
         *
         * @return An instance of SvgConverter.
         */
        fun build() = SvgConverter(
            inputFolder = inputFolder,
            outputFolder = outputFolder,
            generator = SvgGenerator(),
            rasterizer = SvgRasterizer(),
            useSvgGenerator = useSvgGenerator,
            useSvgRasterizer = useSvgRasterizer,
            progressListener = progressListener,
            batchNumber = batchNumber,
        )
    }
}

data class ProgressData(
    val totalDownloadedImages: Int,
    val currentSvgConverted: Int,
    val currentScaledImages: Int
)

open class ProgressListener {
    open suspend fun onProgress(progressData: ProgressData) {
    }
}
