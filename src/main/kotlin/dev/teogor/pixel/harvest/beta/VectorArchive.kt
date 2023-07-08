package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import java.io.File

fun convertSvgToEps(svgFilePath: String, aiFilePath: String) {
    val inkscapePath = "C:\\Program Files\\Inkscape\\bin\\inkscape.exe"
    val svgFile = File(svgFilePath)
    val aiFile = File(aiFilePath)

    val processBuilder = ProcessBuilder(
        inkscapePath,
        svgFile.absolutePath,
        "--export-filename",
        aiFile.absolutePath
    )

    val process = processBuilder.start()
    val errorStream = process.errorStream.bufferedReader()
    val errorMessage = errorStream.readText()
    val exitCode = process.waitFor()

    if (exitCode == 0) {
        println("Conversion successful.")
        println(aiFile.absolutePath)
    } else {
        println("Error message from Inkscape: $errorMessage")
        println("Conversion failed with exit code: $exitCode")
    }
}

fun main() {
    println(PathUtils.getDownloadsFolderPath())
//    val fileName = "A_cute_girl_Azumanga_Daioh_style_baby_Blue_blank_background_in_the_distanc_0001"
//    val baseDownloadPath = "PixelHarvest\\ZeoAI-Automation\\images"
//    val rootPath = "${PathUtils.getDownloadsFolderPath()}\\$baseDownloadPath"
//    val svgFilePath = "$rootPath\\$fileName.svg"
//    val aiFilePath = "$rootPath\\$fileName.eps"
//    println(svgFilePath)
//    convertSvgToEps(svgFilePath, aiFilePath)
}
