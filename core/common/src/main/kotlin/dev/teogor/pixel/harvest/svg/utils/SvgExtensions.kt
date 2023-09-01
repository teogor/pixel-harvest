package dev.teogor.pixel.harvest.svg.utils

import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.w3c.dom.Document
import org.w3c.dom.svg.SVGDocument
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Loads an SVG document from a file.
 *
 * @param file The file representing the SVG document.
 * @return The loaded SVG document.
 */
fun File.loadSvgDocument(): SVGDocument {
    val factory = SAXSVGDocumentFactory(null)
    return factory.createDocument(this.toURI().toString()) as SVGDocument
}

/**
 * Scales an SVG document by the specified scale factor.
 *
 * @param document The SVG document to scale.
 * @param scale    The scale factor.
 * @return The scaled SVG document.
 */
fun Document.scaleSvgDocument(name: String, scale: Double): SVGDocument {
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setParameter("scale", scale)

    val source = DOMSource(this)

    val outputStream = ByteArrayOutputStream()
    val result = StreamResult(outputStream)

    transformer.transform(source, result)

    val scaledSvgBytes = outputStream.toByteArray()
    val scaledSvgTempFile = File("$name.temp.svg")
    FileOutputStream(scaledSvgTempFile).use { it.write(scaledSvgBytes) }

    val scaledDocument = scaledSvgTempFile.loadSvgDocument()

    // Delete the temporary file
    scaledSvgTempFile.delete()

    return scaledDocument
}

/**
 * Retrieves the dimensions of an SVG document.
 *
 * @param svgDocument The SVG document.
 * @return The dimensions of the SVG document.
 * @throws IllegalArgumentException if the dimensions cannot be determined.
 */
fun SVGDocument.getSvgDimensions(): SvgDimensions {
    val svgRoot = this.documentElement

    val viewBox = svgRoot.getAttribute("viewBox")
    if (viewBox.isNotEmpty()) {
        val viewBoxValues = viewBox.split(" ")
        if (viewBoxValues.size == 4) {
            val width = viewBoxValues[2].toFloatOrNull()
            val height = viewBoxValues[3].toFloatOrNull()

            if (width != null && height != null && height != 0f) {
                val aspectRatio = width / height
                return SvgDimensions(width = width, height = height, aspectRatio = aspectRatio)
            }
        }
    }

    throw IllegalArgumentException("Unable to determine the dimensions of the SVG document.")
}