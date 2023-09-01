package dev.teogor.pixel.harvest.utils

import dev.teogor.pixel.harvest.utils.ParamsData.Companion.formatParamsData

fun main() {
    val input = "locking here::2 --upbeta --q 2 --s3 750, 245 --ar 19:6 --stylize 700 --no text ride --v 5.1"
    println(input.getParams())
    println(input.getParams().formatParamsData(delimiter = " ", wrapKeys = true))
    println(input.getParams().formatParamsData(delimiter = " ", wrapKeys = false))
    println(input.getParams().formatParamsData(prefix = "--"))
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
        private fun createParamArg(key: String, value: String?): ParamArg {
            return if (value.isNullOrEmpty()) {
                ParamArg.keyOnly(key)
            } else {
                ParamArg.keyValue(key).apply {
                    isFound = true
                    this.value = value
                }
            }
        }

        fun generateFor(
            hasUpbeta: Boolean,
            quality: String?,
            style: String?,
            version: String?,
            ar: String?,
            stylize: String?,
            no: String?
        ): ParamsData {
            return ParamsData(
                upbeta = ParamArg.keyOnly("upbeta").apply {
                    isFound = hasUpbeta
                },
                quality = createParamArg("quality", quality),
                style = createParamArg("style", style),
                version = createParamArg("version", version),
                ar = createParamArg("ar", ar),
                stylize = createParamArg("stylize", stylize),
                no = createParamArg("no", no)
            )
        }

        fun ParamsData.formatParamsData(
            delimiter: String = " ",
            wrapKeys: Boolean = false,
            prefix: String = ""
        ): String {
            val formattedString = StringBuilder()

            with(formattedString) {
                with(this@formatParamsData) {
                    if (upbeta.isFound) {
                        append(formatKey("upbeta", wrapKeys, prefix)).append(delimiter)
                    }
                    if (quality.isFound) {
                        append(formatKeyWithValue("quality", quality.value, wrapKeys, prefix)).append(delimiter)
                    }
                    if (style.isFound) {
                        append(formatKeyWithValue("style", style.value, wrapKeys, prefix)).append(delimiter)
                    }
                    if (version.isFound) {
                        append(formatKeyWithValue("version", version.value, wrapKeys, prefix)).append(delimiter)
                    }
                    if (ar.isFound) {
                        append(formatKeyWithValue("ar", ar.value, wrapKeys, prefix)).append(delimiter)
                    }
                    if (stylize.isFound) {
                        append(formatKeyWithValue("stylize", stylize.value, wrapKeys, prefix)).append(delimiter)
                    }
                    if (no.isFound) {
                        append(formatKeyWithValue("no", no.value, wrapKeys, prefix)).append(delimiter)
                    }
                }
            }

            return formattedString.toString()
        }

        private fun formatKey(key: String, wrapKeys: Boolean, prefix: String): String {
            val formattedKey = if (wrapKeys) {
                "**$key**"
            } else {
                key
            }
            return "$prefix$formattedKey"
        }

        private fun formatKeyWithValue(key: String, value: String, wrapKeys: Boolean, prefix: String): String {
            val formattedKey = formatKey(key, wrapKeys, prefix)
            return "$formattedKey $value"
        }


    }
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
