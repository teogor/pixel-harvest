package dev.teogor.pixel.harvest.test

import dev.teogor.pixel.harvest.utils.extractFilename
import java.io.File

object ContentTrimmerTest : Test() {

    override fun begin() {
        val contents = listOf(
            "**futuristic landscape black background, and with few purple details realistic, 4k, wide --v 5 --upbeta --q 2 --s 0 --ar 9:16** - Image #1 <@626721635860480021>",
            "**abstract colorful painting, vibrant colors --upbeta --q 2 --s 750** - Image #2 <@626721635860480021>",
            "**nature sunrise with mountains and lake --v 3 --upbeta --s 1080** - Image #3 <@626721635860480021>",
            "**no-dashes** - Image #4 <@626721635860480021>",
            "no asterix - Image #5 <@626721635860480021>",
            "*content - with multiple dashes - second - Image #6 <@626721635860480021>",
            "**<https://s.mj.run/JF7qokCoUbo> geometric futuristic surreal landscape --v 5 --upbeta --q 2 --s 750** - Image #7 <@626721635860480021>",
            "**<https://s.mj.run/JF7qokCoUbo>, <https://s.mj.run/JF7qokCoUbo>, <https://s.mj.run/JF7qokCoUbo> geometric futuristic surreal landscape --v 5 --upbeta --q 2 --s 750** - Image #8 <@626721635860480021>",
            "**<https://s.mj.run/Cv5ngInV2Dw> <https://s.mj.run/PmOuoOY47oY> <https://s.mj.run/oqJWtFdfJh4> at the beach, cool, 4k anime --upbeta --q 2 --s 750** - <@626721635860480021> (relaxed)",
            "**<https://s.mj.run/Cv5ngInV2Dw> <https://s.mj.run/PmOuoOY47oY> <https://s.mj.run/oqJWtFdfJh4> at the beach, cool, 4k anime, In this updated version, the getMaxLengthForFileName() function determines the maximum file name length based on the operating system. The specific values used are common defaults for Windows, macOS, and Linux. You can adjust them if needed, Now, when creating a file using createUniqueFile(), the file name is sanitized and truncated using the sanitizeFileName() function, taking into account the maximum file name length determined by the operating system. I apologize for any confusion caused, and I hope this updated solution meets your requirements. To handle file name length limitations based on the operating system, you can use different approaches. Here's an updated version of the code that uses platform-specific methods to get the maximum file name length: --upbeta --q 2 --s 750** - <@626721635860480021> (relaxed)",
            "*test system file name ; : , . Ä Ö # ' + * ~ `´ ? \\ ß § - Image #1 <@626721635860480021>"
        )

        contents.forEachIndexed { index, content ->
            println("Content ${index + 1}: $content")
            val contentMsg = "${content.extractFilename} (${324.toString().padStart(4, '0')})"
            println("Extracted Text ${index + 1} (L=${contentMsg.length}): ${contentMsg}")
            println()
        }

        val playground = File("downloads/images/ZeoAi-Automation/playground")
        val count = playground.countFiles("teogor_at_the_beach_cool_4k_anime_f804af6a-9d66-422e-a349-f3f84d72f2a3")
        println("next index -> ${(count + 1)}")
    }

    fun File.countFiles(
        name: String,
    ): Int {
        return this.listFiles { file ->
            file.nameWithoutExtension.startsWith(name) && file.isFile
        }?.size ?: 0
    }

    fun File.countDirectories(
        name: String,
    ): Int {
        return this.listFiles { file ->
            file.nameWithoutExtension.startsWith(name) && file.isDirectory
        }?.size ?: 0
    }

}