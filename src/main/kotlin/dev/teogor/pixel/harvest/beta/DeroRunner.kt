package dev.teogor.pixel.harvest.beta

import dev.teogor.pixel.harvest.dero.DeroBuilder
import dev.teogor.pixel.harvest.discord.PathUtils
import java.io.File


suspend fun main() {
    println(PathUtils.getDownloadsFolderPath())
    val rootPath = "${PathUtils.getDownloadsFolderPath()}\\PixelHarvest\\ZeoAI-Automation\\images"
    val deroBuilder = DeroBuilder(
        openThreads = 8,
        targetFolderPath = rootPath,
    )
    deroBuilder.rasterizeSvg()
}
