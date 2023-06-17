package dev.teogor.pixel.harvest.svg.rasterizer

import dev.teogor.pixel.harvest.svg.utils.ImageExtension
import dev.teogor.pixel.harvest.svg.utils.getSvgDimensions
import dev.teogor.pixel.harvest.svg.utils.loadSvgDocument
import dev.teogor.pixel.harvest.svg.utils.scaleSvgDocument
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.TranscodingHints
import org.apache.batik.transcoder.image.ImageTranscoder
import org.w3c.dom.svg.SVGDocument
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

/**
 * Class responsible for rasterizing SVG files.
 */
class SvgRasterizer {

    /**
     * Converts an SVG file to an image using the specified options.
     *
     * @param svgFile The SVG file to convert.
     * @return The result of the rasterization process.
     */
    fun convert(svgFile: SvgFile): RasterizerResult {
        // Start measuring the time
        val startTime = System.currentTimeMillis()
        val fileInfo = RasterizerResult.FileInfo(
            inputFile = svgFile.inputFile,
            outputFile = svgFile.outputFile,
        )
        try {
            // Load the SVG document
            val loadStartTime = System.currentTimeMillis()
            val svgDocument = svgFile.inputFile.loadSvgDocument()
            val loadEndTime = System.currentTimeMillis()
            val loadTime = loadEndTime - loadStartTime

            // Scale the SVG document
            val scaleStartTime = System.currentTimeMillis()
            val scaledDocument = svgDocument.scaleSvgDocument(svgFile.inputFile.nameWithoutExtension, svgFile.scaleFactor)
            val scaleEndTime = System.currentTimeMillis()
            val scaleTime = scaleEndTime - scaleStartTime

            // Save the scaled SVG as image
            val saveStartTime = System.currentTimeMillis()
            saveImage(scaledDocument, svgFile.outputFile, svgFile.extension, svgFile.scaleFactor)
            val saveEndTime = System.currentTimeMillis()
            val saveTime = saveEndTime - saveStartTime

            val dimensions = svgDocument.getSvgDimensions()

            // End measuring the time
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            return RasterizerResult.Success(
                width = dimensions.width,
                height = dimensions.height,
                aspectRatio = dimensions.aspectRatio,
                scaleFactor = svgFile.scaleFactor,
                totalTime = totalTime,
                loadTime = loadTime,
                scaleTime = scaleTime,
                saveTime = saveTime,
                fileInfo = fileInfo,
            )
        } catch (e: Exception) {
            return RasterizerResult.Error(
                errorMessage = e.message ?: "",
                fileInfo = fileInfo,
            )
        }
    }

    private fun saveImage(
        svgDocument: SVGDocument,
        outputFile: File,
        extension: ImageExtension,
        scaleFactor: Double
    ) {
        val transcoder = when (extension) {
            ImageExtension.PNG -> createPngTranscoder(outputFile)
            ImageExtension.JPEG, ImageExtension.JPG -> createJpegTranscoder(outputFile)
        }

        val dimensions = svgDocument.getSvgDimensions()

        // Set the width and height of the output image
        val aspectRatio = dimensions.aspectRatio
        val desiredWidth = (dimensions.width * scaleFactor).toFloat()
        val desiredHeight = calculateHeightFromAspectRatio(aspectRatio, desiredWidth)

        val hints = TranscodingHints()
        hints[ImageTranscoder.KEY_WIDTH] = desiredWidth
        hints[ImageTranscoder.KEY_HEIGHT] = desiredHeight
        transcoder.transcodingHints = hints

        val source = TranscoderInput(svgDocument)
        val output = TranscoderOutput(FileOutputStream(outputFile).use { it })

        try {
            transcoder.transcode(source, output)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createPngTranscoder(outputFile: File): ImageTranscoder {
        return object : ImageTranscoder() {
            override fun createImage(width: Int, height: Int): BufferedImage {
                return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            }

            override fun writeImage(img: BufferedImage?, output: TranscoderOutput?) {
                if (output != null && img != null) {
                    try {
                        ImageIO.write(img, "png", outputFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun createJpegTranscoder(outputFile: File): ImageTranscoder {
        return object : ImageTranscoder() {
            override fun createImage(width: Int, height: Int): BufferedImage {
                return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            }

            override fun writeImage(img: BufferedImage?, output: TranscoderOutput?) {
                if (output != null && img != null) {
                    try {
                        val jpegEncoder = ImageIO.getImageWritersByFormatName("jpeg").next()
                        val jpegParams = jpegEncoder.defaultWriteParam
                        jpegParams.compressionMode = ImageWriteParam.MODE_EXPLICIT
                        jpegParams.compressionQuality = 1f

                        FileOutputStream(outputFile).use { jpegOutput ->
                            jpegEncoder.output = ImageIO.createImageOutputStream(jpegOutput)
                            jpegEncoder.write(null, IIOImage(img, null, null), jpegParams)
                            jpegEncoder.dispose()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}