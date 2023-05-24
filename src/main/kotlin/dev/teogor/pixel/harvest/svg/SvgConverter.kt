package dev.teogor.pixel.harvest.svg

import dev.teogor.pixel.harvest.svg.generator.SvgGenerator
import dev.teogor.pixel.harvest.svg.generator.VectorizerAiToken
import dev.teogor.pixel.harvest.svg.rasterizer.SvgFile
import dev.teogor.pixel.harvest.svg.rasterizer.SvgRasterizer
import dev.teogor.pixel.harvest.svg.rasterizer.processResult
import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import dev.teogor.pixel.harvest.svg.utils.createDirectoryIfNotExists
import dev.teogor.pixel.harvest.svg.utils.formatDuration
import dev.teogor.pixel.harvest.svg.utils.generateFileNameTemplate
import dev.teogor.pixel.harvest.svg.utils.generateRandomNumber
import dev.teogor.pixel.harvest.svg.utils.getFormattedDate
import dev.teogor.pixel.harvest.svg.utils.getFormattedDate2
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import kotlinx.coroutines.runBlocking
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ClassicHttpResponse
import java.io.File
import java.io.FileWriter

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
    private val useSvgGenerator: Boolean,
    private val useSvgRasterizer: Boolean,
    private val includeDataset: Boolean,
    private val progressListener: ProgressListener,
    private val batchNumber: Int,
) {

    private val generatorInputDirectory: String = if (useSvgGenerator) {
        inputFolder.path
    } else {
        ""
    }
    private val generatorOutputDirectory: String = if (useSvgGenerator) {
        "${outputFolder.path}/set-${batchNumber.toString().padStart(6, '0')}"
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
            "$generatorOutputDirectory/scaled"
        } else {
            outputFolder.path
        }
    } else {
        ""
    }
    val reportFile = File("$generatorOutputDirectory/report_${getFormattedDate()}.md")

    init {
        initializeConversion()
    }


    /**
     * Initializes the conversion process based on the selected options.
     */
    private fun initializeConversion() {
        if (useSvgGenerator && useSvgRasterizer) {
            generateSvg()
            rasterizeSvg()
        } else if (useSvgGenerator) {
            generateSvg()
        } else if (useSvgRasterizer) {
            rasterizeSvg()
        }
        val progressData = ProgressData(
            currentIndex = 0,
            processingStep = ProcessingStep.DONE,
        )
        dispatchProgress(
            progressData = progressData,
            currentIndex = 0,
        )
    }

    /**
     * Converts images to SVG using the SvgGenerator.
     */
    private fun generateSvg() {
        val directoryInput = File(generatorInputDirectory)
        val directoryExportSvg = "$generatorOutputDirectory/svg".createDirectoryIfNotExists()
        val directoryExportImage = "$generatorOutputDirectory/original".createDirectoryIfNotExists()

        if (!directoryExportSvg.exists()) {
            directoryExportSvg.mkdir()
        }
        if (!directoryExportImage.exists()) {
            directoryExportImage.mkdir()
        }

        FileWriter(reportFile, false).use { writer ->
            writer.write("# ICR (${getFormattedDate2()})  \n")
            writer.write("###### *ICR - Image Conversion Report  \n\n")
        }

        var currentFileIndex = 0
        val imageExtensions = listOf("jpg", "jpeg", "png")
        val progressData = ProgressData(
            currentIndex = currentFileIndex,
            processingStep = ProcessingStep.GENERATING_SVG,
        )
        dispatchProgress(
            progressData = progressData,
            currentIndex = currentFileIndex,
        )
        currentFileIndex++
        directoryInput.listFilesWithExtensions(imageExtensions) { imageFile ->
            val request = Request.post("https://vectorizer.ai/api/v1/vectorize")
                .addHeader(
                    "Authorization",
                    "Basic $VectorizerAiToken"
                )
                .body(
                    MultipartEntityBuilder.create()
                        .addBinaryBody("image", imageFile)
                        .build()
                )
            val response = request.execute().returnResponse() as ClassicHttpResponse

            if (response.code == 200) {
                val timestamp = getFormattedDate()
                val randomNumber = generateRandomNumber()
                val fileNameDataSet = "dataset_${timestamp}_$randomNumber"
                val fileName = imageFile.nameWithoutExtension
                val regex = Regex("(.+) \\(\\d+\\)")
                val matchResult = regex.find(fileName)
                val extractFileName = matchResult?.groupValues?.get(1) ?: fileName
                val index = directoryExportImage.countFiles(extractFileName)
                val fileNamePad = if (index == 0) {
                    extractFileName
                } else {
                    "$extractFileName (${index.toString().padStart(4, '0')})"
                }
                val outputFileSvg = File("${directoryExportSvg}/${fileNamePad}.svg")
                val outputFileImage = File("${directoryExportImage}/${fileNamePad}.${imageFile.extension}")
                imageFile.copyTo(outputFileImage)
                response.entity.writeTo(outputFileSvg.outputStream())
                response.close()

                FileWriter(reportFile, true).use { writer ->
                    writer.write("### ${imageFile.nameWithoutExtension}  \n")
                    writer.write("${outputFileSvg.nameWithoutExtension}: [SVG](${outputFileSvg.absoluteFile}), [${outputFileImage.extension.uppercase()}](${outputFileImage.absoluteFile})  \n")
                }

                // Delete the old file
                if (imageFile.exists()) {
                    // todo once the **model** is fine remove this
                    // imageFile.delete()
                }
                dispatchProgress(
                    progressData = progressData,
                    currentIndex = currentFileIndex,
                )
                currentFileIndex++
            }
        }
    }

    private fun dispatchProgress(
        progressData: ProgressData,
        currentIndex: Int,
    ) {
        runBlocking {
            progressListener.onProgress(
                progressData.copy(
                    currentIndex = currentIndex
                )
            )
        }
    }

    /**
     * Converts SVG files to images using the SvgRasterizer.
     */
    private fun rasterizeSvg() {
        val directoryInput = File(rasterizerInputDirectory)
        val directoryExport = File(rasterizerOutputDirectory)
        val saveExtension = ImageExtension.JPEG

        var totalProcessingTime = 0L
        var totalLoadTime = 0L
        var totalScaleTime = 0L
        var totalSaveTime = 0L
        var processedCount = 0

        var currentFileIndex = 0
        val progressData = ProgressData(
            currentIndex = currentFileIndex,
            processingStep = ProcessingStep.SCALING_SVG,
        )
        dispatchProgress(
            progressData = progressData,
            currentIndex = currentFileIndex,
        )
        currentFileIndex++
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
            dispatchProgress(
                progressData = progressData,
                currentIndex = currentFileIndex,
            )
            currentFileIndex++
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
        private var includeDataset: Boolean = false
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
         * Sets whether to include dataset for AI training.
         *
         * @param enabled Flag indicating whether to include dataset for AI training.
         * @return The Builder instance.
         */
        fun withIncludeDataset(enabled: Boolean) = apply {
            this.includeDataset = enabled
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
            includeDataset = includeDataset,
            progressListener = progressListener,
            batchNumber = batchNumber,
        )
    }
}

data class ProgressData(
    val currentIndex: Int,
    val processingStep: ProcessingStep,
)

enum class ProcessingStep {
    PARSING,
    GENERATING_SVG,
    SCALING_SVG,
    DONE
}

open class ProgressListener {
    open suspend fun onProgress(progressData: ProgressData) {
    }
}
