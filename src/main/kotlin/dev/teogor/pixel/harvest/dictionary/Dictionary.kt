package dev.teogor.pixel.harvest.dictionary

open class Dictionary internal constructor(
    open val list: Set<String> = emptySet()
) {
    open class Builder {
        private val setType = mutableSetOf<Type>()

        fun addType(colorType: Type): Builder {
            setType.add(colorType)
            return this
        }

        open fun build(): Dictionary {
            val includedSets = setType.map { it.getSet() }
            val mergedSet = includedSets.flatten().toSet()
            return Dictionary(mergedSet)
        }
    }


    interface Type {
        fun getSet(): Set<String>
    }

    @Deprecated(message = "to be removed once the builder is completed")
    open val listLegacy: Set<String> = emptySet()

    fun getRandomUniqueItems(count: Int): List<String> {
        require(count <= listLegacy.size) { "Requested count exceeds the available unique items." }

        return listLegacy.shuffled().distinct().take(count)
    }

    open fun getEntry(item: String): String? {
        return listLegacy.find { it.equals(item, ignoreCase = true) }
    }
}