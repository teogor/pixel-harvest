package dev.teogor.pixel.harvest.svg

import dev.teogor.pixel.harvest.svg.generator.SvgGenerator
import dev.teogor.pixel.harvest.svg.generator.VectorizerAiToken
import dev.teogor.pixel.harvest.svg.rasterizer.SvgFile
import dev.teogor.pixel.harvest.svg.rasterizer.SvgRasterizer
import dev.teogor.pixel.harvest.svg.rasterizer.processResult
import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import dev.teogor.pixel.harvest.svg.utils.createDirectoryIfNotExists
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
import kotlin.math.max

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
 * @param includeDataset   Flag indicating whether to include dataset for AI training.
 * @param splitEnabled     Flag indicating whether to split generated images into SVG and scaled versions.
 *                         Images will alternate between SVG and scaled versions based on index.
 * @param progressListener The progress listener for tracking conversion progress.
 * @param batchNumber      The batch number for the converted images.
 */
@Deprecated(message = "Use DeroBuilder")
class SvgConverter private constructor(
    private val inputFolder: File,
    private val outputFolder: File,
    private val generator: SvgGenerator,
    private val rasterizer: SvgRasterizer,
    private val useSvgGenerator: Boolean,
    private val useSvgRasterizer: Boolean,
    private val includeDataset: Boolean,
    private val splitEnabled: Boolean,
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
            "${outputFolder.parentFile.parentFile.path}/scaled"
        }
    } else {
        ""
    }
    private val splitImagesOutputDirectory: String = if (splitEnabled) {
        if (useSvgGenerator) {
            "$generatorOutputDirectory/split"
        } else {
            "${outputFolder.parentFile.parentFile.path}/split"
        }
    } else {
        ""
    }
    private val reportFile = File("$generatorOutputDirectory/report_${getFormattedDate()}.md")
    private val outputLines = mutableListOf<ImageReport>()

    private sealed class ImageReport(
        val name: String,
        val svgFile: File,
        val originalFile: File,
        val scaledFile: File,
    ) {

        class SvgGeneratorReport(
            name: String,
            svgFile: File,
            originalFile: File,
        ) : ImageReport(
            name,
            svgFile,
            originalFile,
            File("")
        )

        class SvgRasterizerReport(
            name: String,
            svgFile: File,
            scaledFile: File,
            originalFile: File,
        ) : ImageReport(
            name,
            svgFile,
            originalFile,
            scaledFile,
        ) {
            companion object {
                fun from(imageReport: ImageReport, svgFile: File): SvgRasterizerReport {
                    return SvgRasterizerReport(
                        imageReport.name,
                        imageReport.svgFile,
                        imageReport.scaledFile,
                        svgFile
                    )
                }
            }
        }

    }

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
            prepareSplitImages()
        } else if (useSvgGenerator) {
            generateSvg()
        } else if (useSvgRasterizer) {
            rasterizeSvg()
        }
        dispatchProgress(
            progressData = ProgressData(
                currentIndex = 0,
                processingStep = ProcessingStep.DONE,
            ),
            currentIndex = 0,
        )

        if (useSvgGenerator && useSvgRasterizer) {
            FileWriter(reportFile, false).use { writer ->
                writer.write("# ICR (${getFormattedDate2()})  \n")
                writer.write("###### *ICR - Image Conversion Report  \n\n")
            }

            outputLines.forEach {
                FileWriter(reportFile, true).use { writer ->
                    writer.write("### ${it.name}  \n")
                    writer.write("Download Variant Locations: [SVG](${it.svgFile.absoluteFile}), [ORIGINAL (*${it.originalFile.extension.uppercase()})](${it.originalFile.absoluteFile}), [Scaled (*${it.scaledFile.extension.uppercase()})](${it.scaledFile.absoluteFile})  \n")
                }
            }
        }
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
        // todo try to make multiple requests
        // todo read the docs. it would be better if we can make around 4 requests at a time
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
                val extractFileName = (matchResult?.groupValues?.get(1) ?: fileName).replace(" ", "_")
                val index = directoryExportImage.countFiles(extractFileName) + 1
                val fileNamePad = "${extractFileName}_${index.toString().padStart(4, '0')}"
                val outputFileSvg = File("${directoryExportSvg}/${fileNamePad}.svg")
                val outputFileImage = File("${directoryExportImage}/${fileNamePad}.${imageFile.extension}")
                imageFile.copyTo(outputFileImage)
                response.entity.writeTo(outputFileSvg.outputStream())
                response.close()

                FileWriter(reportFile, true).use { writer ->
                    writer.write("### ${imageFile.nameWithoutExtension}  \n")
                    val outputFileSvgPath = outputFileSvg.absolutePath
                    val outputFileImagePath = outputFileImage.absolutePath
                    writer.write("Download Variant Locations:  [SVG]($outputFileSvgPath), [${outputFileImage.extension.uppercase()}]($outputFileImagePath)  \n")

                    outputLines.add(
                        ImageReport.SvgGeneratorReport(
                            name = imageFile.nameWithoutExtension,
                            svgFile = outputFileSvg,
                            originalFile = outputFileImage
                        )
                    )
                }

                if (imageFile.exists()) {
                    imageFile.delete()
                }
                dispatchProgress(
                    progressData = progressData,
                    currentIndex = currentFileIndex,
                )
                currentFileIndex++
            }
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
                    // fileNamePattern = { fileName ->
                    //     val lastIndex = fileName.lastIndexOf("_")
                    //     if (lastIndex != -1) {
                    //         val fileCount = (processedCount + 1).toString().padStart(3, '0')
                    //         val fileNameStr = fileName.substring(0, lastIndex).replace("teogor_", "")
                    //
                    //         val filename = "\$fileName_converted-\$fileCount"
                    //         val tFilename = filename.generateFileNameTemplate(
                    //             fileName = fileNameStr,
                    //             fileCount = fileCount,
                    //         )
                    //         tFilename
                    //     } else {
                    //         fileName
                    //     }
                    // }
                )
            )
            conversionResult.processResult(
                onSuccess = {
                    with(it) {
                        // println("SVG scaled and saved successfully as ${fileInfo.outputFile.name}!")
                        // println("Processing Time: ${totalTime.formatDuration()}")
                        // println("Loading Time:    ${loadTime.formatDuration()}")
                        // println("Scaling Time:    ${scaleTime.formatDuration()}")
                        // println("Saving Time:     ${saveTime.formatDuration()}")
                        // println("")
                        totalProcessingTime += totalTime
                        totalLoadTime += loadTime
                        totalScaleTime += scaleTime
                        totalSaveTime += saveTime
                        processedCount++
                        if (useSvgGenerator && useSvgRasterizer) {
                            outputLines[currentFileIndex - 1] = ImageReport.SvgRasterizerReport.from(
                                outputLines[currentFileIndex - 1],
                                fileInfo.outputFile
                            )
                        }
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

        // val averageProcessingTime = totalProcessingTime / processedCount
        // val averageLoadTime = totalLoadTime / processedCount
        // val averageScaleTime = totalScaleTime / processedCount
        // val averageSaveTime = totalSaveTime / processedCount
        // println("Processed Images: $processedCount")
        // println("Total Processing Time: ${totalProcessingTime.formatDuration()}")
        // println("Total Loading Time:    ${totalLoadTime.formatDuration()}")
        // println("Total Scaling Time:    ${totalScaleTime.formatDuration()}")
        // println("Total Saving Time:     ${totalSaveTime.formatDuration()}")
        // println("Average Processing Time: ${averageProcessingTime.formatDuration()}")
        // println("Average Loading Time:    ${averageLoadTime.formatDuration()}")
        // println("Average Scaling Time:    ${averageScaleTime.formatDuration()}")
        // println("Average Saving Time:     ${averageSaveTime.formatDuration()}")
    }

    private fun prepareSplitImages() {
        if (!splitEnabled) {
            return
        }

        val svgDirectory = File(rasterizerInputDirectory)
        val scaledDirectory = File(rasterizerOutputDirectory)
        val outputDirectory = File(splitImagesOutputDirectory)

        if (!svgDirectory.exists() || !scaledDirectory.exists()) {
            return
        }

        val svgFiles = svgDirectory.listFiles() ?: emptyArray()
        val scaledFiles = scaledDirectory.listFiles() ?: emptyArray()

        val maxIndex = max(svgFiles.size, scaledFiles.size)

        for (index in 0 until maxIndex) {
            val svgFile = svgFiles.getOrNull(index)
            val scaledFile = scaledFiles.getOrNull(index)

            if (index % 2 == 0) {
                if (svgFile != null) {
                    val outputSvgFile = File(outputDirectory, svgFile.name)
                    svgFile.copyTo(outputSvgFile, overwrite = true)
                }
            } else {
                if (scaledFile != null) {
                    val outputScaledFile = File(outputDirectory, scaledFile.name)
                    scaledFile.copyTo(outputScaledFile, overwrite = true)
                }
            }
        }
    }


    private fun dispatchProgress(
        progressData: ProgressData,
        currentIndex: Int,
    ) {
        runBlocking {
            try {
                progressListener.onProgress(
                    progressData.copy(
                        currentIndex = currentIndex
                    )
                )
            } catch (_: Error) {

            }
        }
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
        private var splitEnabled: Boolean = false
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
         * Sets whether to split generated images into SVG and scaled versions, alternating by index.
         *
         * @param enabled Flag indicating whether to enable splitting of images.
         * @return The Builder instance.
         */
        fun withSplitEnabled(enabled: Boolean) = apply {
            this.splitEnabled = enabled
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
            splitEnabled = splitEnabled,
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
