package dev.teogor.pixel.harvest.dero

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.ProcessingStep
import dev.teogor.pixel.harvest.svg.ProgressData
import dev.teogor.pixel.harvest.svg.generator.VectorizerAiToken
import dev.teogor.pixel.harvest.svg.rasterizer.SvgFile
import dev.teogor.pixel.harvest.svg.rasterizer.SvgRasterizer
import dev.teogor.pixel.harvest.svg.rasterizer.processResult
import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import dev.teogor.pixel.harvest.svg.utils.getFilesWithExtensions
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countDirectories
import dev.teogor.pixel.harvest.test.ContentTrimmerTest.countFiles
import kotlinx.coroutines.*
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ClassicHttpResponse
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.random.Random

fun main() {
    val folderPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images"

    runBlocking {
        val deroListener: DeroBuilder.Listener = object : DeroBuilder.Listener() {
            override suspend fun onProgress(progressData: ProgressData) {
                println(progressData)
            }
        }
        val deroBuilder = DeroBuilder(
            folderPath,
            deroListener
        )

        if (!deroBuilder.copyFilesToNewFolder()) {
            println("error at renaming")
        }
        if (!deroBuilder.renameFiles()) {
            println("error at renaming")
        }
        if (!deroBuilder.generateSvg()) {
            println("error at converting SVGs")
        }
        if (!deroBuilder.rasterizeSvg()) {
            println("error at rasterize SVGs")
        }
        if (!deroBuilder.createSplitDataset()) {
            println("error at rasterize SVGs")
        }
        println("Done!")
    }
}

class DeroPaths {
    val processedPath = "processed"
    val svgPath = "svg"
    val originalPath = "original"
    val scaledPath = "scaled"
    val splitPath = "split"

    fun joinPaths(basePath: String, path: String): String {
        return "$basePath\\$path"
    }

    fun getFolder(path: String): File {
        val folder = File(path)
        folder.mkdirs()
        return folder
    }
}

class DeroBuilder(
    private val targetFolderPath: String,
    private val progressListener: Listener = Listener.EMPTY,
) {

    open class Listener {
        open suspend fun onProgress(progressData: ProgressData) {
        }

        companion object {

            val EMPTY: Listener = object : Listener() {
                override suspend fun onProgress(progressData: ProgressData) {
                    // empty
                }
            }

        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val dispatcher = newFixedThreadPoolContext(4, "DeroBuilder")

    var imagesToProcess: Int = 0

    private val deroPaths = DeroPaths()
    private val rasterizer = SvgRasterizer()

    private val imageExtensions = listOf("jpg", "jpeg", "png")

    // paths
    private val jobFolderPath: String
    private val processedFolderPath: String
    private val setFolderPath: String
    private val svgFolderPath: String
    private val originalFolderPath: String
    private val scaledFolderPath: String
    private val splitFolderPath: String

    // folders
    private val jobFolder: File
    private val processedFolder: File
    private val setFolder: File
    private val svgFolder: File
    private val originalFolder: File
    private val scaledFolder: File
    private val splitFolder: File

    init {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM_dd_yyyy_HHmmss"))
        val randomNumber = Random.nextInt(1000, 10000)
        val newFolderName = "batch_job_$timestamp" + "_$randomNumber"
        jobFolderPath = "$targetFolderPath\\$newFolderName"

        jobFolder = File(jobFolderPath)
        jobFolder.mkdirs()

        processedFolderPath = deroPaths.joinPaths(targetFolderPath, deroPaths.processedPath)
        processedFolder = File(processedFolderPath)
        processedFolder.mkdirs()

        val batchNumber = processedFolder.countDirectories("set") + 1
        setFolderPath = deroPaths.joinPaths(processedFolderPath, "set-${batchNumber.toString().padStart(6, '0')}")
        setFolder = File(setFolderPath)
        setFolder.mkdirs()

        svgFolderPath = deroPaths.joinPaths(setFolderPath, deroPaths.svgPath)
        originalFolderPath = deroPaths.joinPaths(setFolderPath, deroPaths.originalPath)
        scaledFolderPath = deroPaths.joinPaths(setFolderPath, deroPaths.scaledPath)
        splitFolderPath = deroPaths.joinPaths(setFolderPath, deroPaths.splitPath)

        svgFolder = deroPaths.getFolder(svgFolderPath)
        originalFolder = deroPaths.getFolder(originalFolderPath)
        scaledFolder = deroPaths.getFolder(scaledFolderPath)
        splitFolder = deroPaths.getFolder(splitFolderPath)
    }

    suspend fun copyFilesToNewFolder(): Boolean = withContext(Dispatchers.IO) {
        val sourceFolder = File(targetFolderPath)
        sourceFolder.listFilesWithExtensions(imageExtensions) { file ->
            val destinationFile = File("$jobFolderPath/${file.name}")
            // todo replace to this once debug is done
            file.renameTo(destinationFile)
            // file.copyTo(destinationFile)
        }

        imagesToProcess = sourceFolder.countFiles("")

        return@withContext true
    }

    suspend fun renameFiles(): Boolean = withContext(Dispatchers.IO) {
        jobFolder.listFilesWithExtensions(imageExtensions) { file ->
            val fileName = file.nameWithoutExtension
            val regex = Regex("(.+) \\(\\d+\\)")
            val matchResult = regex.find(fileName)
            val extractFileName = (matchResult?.groupValues?.get(1) ?: fileName).replace(" ", "_")
            val index = jobFolder.countFiles(extractFileName) + 1
            val fileNamePad = "${extractFileName}_${index.toString().padStart(4, '0')}"
            val newFile = File(jobFolder, "$fileNamePad.${file.extension}")
            file.renameTo(newFile)
        }

        return@withContext true
    }

    suspend fun generateSvg(): Boolean = withContext(Dispatchers.IO) {
        var currentFileIndex = 0
        val imageExtensions = listOf("jpg", "jpeg", "png")

        val imageFiles = jobFolder.getFilesWithExtensions(imageExtensions)

        val progressData = ProgressData(
            currentIndex = currentFileIndex,
            processingStep = ProcessingStep.GENERATING_SVG,
        )
        progressListener.onProgress(progressData)
        imageFiles.map { imageFile ->
            launch(dispatcher) {
                val fileName = imageFile.nameWithoutExtension
                val fileExtension = imageFile.extension
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
                    val outputFileSvg = File("${svgFolderPath}/${fileName}.svg")
                    val outputFileImage = File("${originalFolderPath}/${fileName}.${fileExtension}")

                    imageFile.copyTo(outputFileImage)
                    response.entity.writeTo(outputFileSvg.outputStream())
                    response.close()

                    if (imageFile.exists()) {
                        imageFile.delete()
                    }
                }
                currentFileIndex++
                progressListener.onProgress(progressData.copy(
                    currentIndex = currentFileIndex
                ))
            }
        }

        return@withContext true
    }

    suspend fun rasterizeSvg(): Boolean = withContext(Dispatchers.IO) {
        val saveExtension = ImageExtension.JPEG

        var totalProcessingTime = 0L
        var totalLoadTime = 0L
        var totalScaleTime = 0L
        var totalSaveTime = 0L
        var processedCount = 0

        var currentFileIndex = 0

        val svgFiles = svgFolder.getFilesWithExtensions(listOf("svg"))

        val progressData = ProgressData(
            currentIndex = currentFileIndex,
            processingStep = ProcessingStep.SCALING_SVG,
        )
        progressListener.onProgress(progressData)
        svgFiles.map { svgFile ->
            launch(dispatcher) {
                val conversionResult = rasterizer.convert(
                    SvgFile(
                        inputFile = svgFile,
                        outputFolder = scaledFolder,
                        scaleFactor = 6.0,
                        extension = saveExtension,
                    )
                )
                conversionResult.processResult(
                    onSuccess = {
                        with(it) {
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
                currentFileIndex++
                progressListener.onProgress(progressData.copy(
                    currentIndex = currentFileIndex
                ))
            }
        }

        return@withContext true
    }

    suspend fun createSplitDataset(): Boolean = withContext(Dispatchers.IO) {
        val svgFiles = svgFolder.listFiles() ?: emptyArray()
        val scaledFiles = scaledFolder.listFiles() ?: emptyArray()

        val maxIndex = max(svgFiles.size, scaledFiles.size)

        for (index in 0 until maxIndex) {
            val svgFile = svgFiles.getOrNull(index)
            val scaledFile = scaledFiles.getOrNull(index)

            if (index % 2 == 0) {
                if (svgFile != null) {
                    val outputSvgFile = File(splitFolder, svgFile.name)
                    svgFile.copyTo(outputSvgFile, overwrite = true)
                }
            } else {
                if (scaledFile != null) {
                    val outputScaledFile = File(splitFolder, scaledFile.name)
                    scaledFile.copyTo(outputScaledFile, overwrite = true)
                }
            }
        }

        return@withContext true
    }
}
