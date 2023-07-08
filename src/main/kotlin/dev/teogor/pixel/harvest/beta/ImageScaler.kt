package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream

enum class Resolution(val width: Int, val height: Int) {
    R4K(3840, 2160),
    R8K(7680, 4320)
}

class ImageScaler private constructor(
    resolution: Resolution,
    directoryPath: String,
    deleteOldFile: Boolean = false,
) {
    data class ImageDimension(val width: Int, val height: Int) {
        val aspectRatio = width.toDouble() / height.toDouble()
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)

    companion object {
        fun forDirectory(
            resolution: Resolution,
            directoryPath: String,
            deleteOldFile: Boolean = false,
        ): ImageScaler {
            return ImageScaler(
                resolution = resolution,
                directoryPath = directoryPath,
                deleteOldFile = deleteOldFile,
            ).apply {
                val imageExtensions = listOf("jpg", "jpeg", "png")
                val directory = File(directoryPath)
                directory.listFilesWithExtensions(imageExtensions) { imageFile ->
                    println("Image File -> Name: ${imageFile.nameWithoutExtension}")

                    val originalImage = ImageIO.read(imageFile)
                    val originalResolution = ImageDimension(originalImage.width, originalImage.height)
                    println(
                        "Previous Resolution: ${originalResolution.width}x${originalResolution.height} (${
                            originalResolution.aspectRatio.format(
                                3
                            )
                        })"
                    )

                    val scaledImage = scaleImage(originalImage, resolution)
                    val newResolution = ImageDimension(scaledImage.width, scaledImage.height)
                    println("New Resolution: ${newResolution.width}x${newResolution.height} (${newResolution.aspectRatio.format(3)})")

                    val outputFileName = "${imageFile.nameWithoutExtension}-scaled.jpg"
                    val outputFilePath = "${imageFile.parentFile}\\$outputFileName"
                    val outputFile = File(outputFilePath)

                    val writer: ImageWriter = ImageIO.getImageWritersByFormatName("jpg").next()
                    val param: ImageWriteParam = writer.defaultWriteParam
                    param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    param.compressionQuality = 1.0f // Set quality to 100%

                    val outputStream: ImageOutputStream = ImageIO.createImageOutputStream(outputFile)
                    writer.output = outputStream
                    writer.write(null, IIOImage(scaledImage, null, null), param)

                    outputStream.close()
                    writer.dispose()

                    if (outputFile.exists() && deleteOldFile) {
                        imageFile.delete()
                    }
                }
            }
        }

        fun forImage(
            resolution: Resolution,
            directoryPath: String,
            deleteOldFile: Boolean = false,
        ): ImageScaler {
            return ImageScaler(
                resolution = resolution,
                directoryPath = directoryPath,
                deleteOldFile = deleteOldFile,
            ).apply {

            }
        }
    }

    fun scaleImage(image: BufferedImage, resolution: Resolution): BufferedImage {
        val aspectRatio = image.width.toDouble() / image.height.toDouble()
        val scaledWidth: Int
        val scaledHeight: Int

        if (aspectRatio > 1) {
            scaledWidth = resolution.width
            scaledHeight = (resolution.width / aspectRatio).toInt()
        } else {
            scaledWidth = (resolution.height * aspectRatio).toInt()
            scaledHeight = resolution.height
        }

        val scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val bufferedImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        return bufferedImage
    }
}

fun main() {
    // sof::customizable
    val resolution = Resolution.R8K
    val directoryPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images\\photo-ai"
    val deleteOldFile = true
    // eof::customizable

    ImageScaler.forDirectory(
        resolution = resolution,
        directoryPath = directoryPath,
        deleteOldFile = deleteOldFile,
    )
}
