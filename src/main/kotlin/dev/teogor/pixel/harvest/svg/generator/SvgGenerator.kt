package dev.teogor.pixel.harvest.svg.generator

import dev.teogor.pixel.harvest.svg.utils.createDirectoryIfNotExists
import dev.teogor.pixel.harvest.svg.utils.generateRandomNumber
import dev.teogor.pixel.harvest.svg.utils.getFormattedDate
import dev.teogor.pixel.harvest.svg.utils.getFormattedDate2
import dev.teogor.pixel.harvest.svg.utils.listFilesWithExtensions
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ClassicHttpResponse
import java.io.File
import java.io.FileWriter

const val VectorizerAiToken =
    "dmt4czc5anJwYWd2NmVmOnRyNnRxdW1wcTljYjc0ZjIwNDMzZTVqNmF0cXJuMjBhNHEwOHIzMDZkcXZpZTNxM2t0a3Y="

/**
 * A utility class for converting images to SVG format using the vectorizer.ai API.
 *
 * @property apiToken The API token for authentication.
 * @property inputDirectory The input directory containing the images to convert.
 * @property outputDirectory The output directory for storing the converted SVG files and the conversion report.
 */
class SvgGenerator {


    var inputDirectory: String = ""
    var outputDirectory: String = ""

    /**
     * Converts the images in the input directory to SVG format using the vectorizer.ai API.
     * The converted SVG files and a conversion report will be saved in the output directory.
     */
    fun convertImages() {
        val directoryInput = File(inputDirectory)
        val directoryExportSvg = "$outputDirectory/svg".createDirectoryIfNotExists()
        val directoryExportImage = "$outputDirectory/image".createDirectoryIfNotExists()
        val reportFile = File("$outputDirectory/report_${getFormattedDate()}.md")

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

        val imageExtensions = listOf("jpg", "jpeg", "png")
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
                val fileName = "dataset_${timestamp}_$randomNumber"
                val outputFileSvg = File("${directoryExportSvg}/${fileName}.svg")
                val outputFileImage = File("${directoryExportImage}/${fileName}.${imageFile.extension}")
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
            }
        }
    }
}