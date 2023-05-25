package dev.teogor.pixel.harvest.utils

fun main() {
    val input = "locking here::2 --upbeta --q 2 --s 750, 245 --ar 19:6 --stylize 700 --no text ride --v 5.1"
    println(formatParamsData(input.getParams()))
}

data class ParamArg(
    val key: String,
    val acceptsValues: Boolean = true,
    var value: String = "",
    var isFound: Boolean = false,
) {
    companion object {
        fun keyOnly(key: String): ParamArg {
            return ParamArg(
                key = key,
                acceptsValues = false,
            )
        }

        fun keyValue(key: String): ParamArg {
            return ParamArg(
                key = key,
            )
        }
    }
}

data class ParamsData(
    val upbeta: ParamArg = ParamArg.keyOnly(
        "upbeta"
    ),
    val quality: ParamArg = ParamArg.keyValue(
        "quality"
    ),
    val style: ParamArg = ParamArg.keyValue(
        "style"
    ),
    val version: ParamArg = ParamArg.keyValue(
        "version"
    ),
    val ar: ParamArg = ParamArg.keyValue(
        "ar"
    ),
    val stylize: ParamArg = ParamArg.keyValue(
        "stylize"
    ),
    val no: ParamArg = ParamArg.keyValue(
        "no"
    ),
) {
    companion object {
        fun generateFor(
            hasUpbeta: Boolean,
            quality: String?,
            style: String?,
            version: String?,
            ar: String?,
            stylize: String?,
            no: String?
        ): ParamsData {
            return ParamsData().apply {
                if (hasUpbeta) {
                    this.upbeta.apply {
                        isFound = true
                    }
                } else {
                    this.upbeta.apply {
                        isFound = false
                    }
                }
                if (quality.isNullOrEmpty()) {
                    this.quality.apply {
                        isFound = false
                    }
                } else {
                    this.quality.apply {
                        isFound = true
                        value = quality
                    }
                }
                if (style.isNullOrEmpty()) {
                    this.style.apply {
                        isFound = false
                    }
                } else {
                    this.style.apply {
                        isFound = true
                        value = style
                    }
                }
                if (version.isNullOrEmpty()) {
                    this.version.apply {
                        isFound = false
                    }
                } else {
                    this.version.apply {
                        isFound = true
                        value = version
                    }
                }
                if (ar.isNullOrEmpty()) {
                    this.ar.apply {
                        isFound = false
                    }
                } else {
                    this.ar.apply {
                        isFound = true
                        value = ar
                    }
                }
                if (stylize.isNullOrEmpty()) {
                    this.stylize.apply {
                        isFound = false
                    }
                } else {
                    this.stylize.apply {
                        isFound = true
                        value = stylize
                    }
                }
                if (no.isNullOrEmpty()) {
                    this.no.apply {
                        isFound = false
                    }
                } else {
                    this.no.apply {
                        isFound = true
                        value = no
                    }
                }
            }
        }
    }
}

fun formatParamsData(paramsData: ParamsData): String {
    val formattedString = StringBuilder()

    with(formattedString) {
        with(paramsData) {
            if (upbeta.isFound) {
                append("**upbeta**\n")
            }
            if (quality.isFound) {
                append("**quality** ${quality.value}\n")
            }
            if (style.isFound) {
                append("**style** ${style.value}\n")
            }
            if (version.isFound) {
                append("**version** ${version.value}\n")
            }
            if (ar.isFound) {
                append("**ar** ${ar.value}\n")
            }
            if (stylize.isFound) {
                append("**stylize** ${stylize.value}\n")
            }
            if (no.isFound) {
                append("**no** ${no.value}\n")
            }
        }
    }

    return formattedString.toString()
}

fun String.getParams(): ParamsData {
    val regex = Regex("\\Q--\\E(\\w+)\\s*([^\\Q-\\E\\s]+)?")
    val matches = regex.findAll(this)

    val params = matches.map {
        val paramName = it.groupValues[1]
        val paramValue = it.groupValues[2].takeIf { value -> value.isNotEmpty() }
        paramName to paramValue
    }.toMap()

    val q = params["q"]
    val s = params["s"]?.split(",")?.map { it.trim() }?.firstOrNull()
    val v = params["v"]
    val ar = params["ar"]
    val stylize = params["stylize"]

    val noRegex = Regex("--no\\s*(.*?)(?=--|$)")
    val noMatch = noRegex.find(this)
    val no = noMatch?.groupValues?.get(1)?.split(",")?.firstOrNull()?.trim()

    return ParamsData.generateFor(
        hasUpbeta = params.containsKey("upbeta"),
        quality = q,
        style = s,
        version = v,
        ar = ar,
        stylize = stylize,
        no = no,
    )
}
