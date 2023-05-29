package dev.teogor.pixel.harvest.dictionary

open class Dictionary internal constructor(
    open val list: Set<String>
) {
    open class Builder {
        private val setBuilder = mutableSetOf<String>()

        fun addSet(set: Set<String>): Builder {
            setBuilder.addAll(set)
            return this
        }

        open fun build(): Dictionary {
            return Dictionary(setBuilder.toSet())
        }
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

    open fun getList(vararg lists: Set<String>): Set<String> {
        return lists.toList().flatten().toSet()
    }
}