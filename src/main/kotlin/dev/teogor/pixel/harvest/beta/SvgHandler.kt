package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.discord.PathUtils
import dev.teogor.pixel.harvest.svg.SvgConverter
import java.io.File

fun main() {
    val baseDownloadPath = "PixelHarvest\\ZeoAI-Automation\\images"
    val rootPath = "${PathUtils.getDownloadsFolderPath()}\\$baseDownloadPath"
    val inputFolder = File(rootPath)
    val outputFolder = File("${rootPath}\\processed")
    SvgConverter.Builder(inputFolder, outputFolder)
        .withSvgGenerator(true)
        .withSvgRasterizer(true)
        .withIncludeDataset(false)
        .withSplitEnabled(true)
        .withBatchNumber(4)
        .build()
}
