package dev.teogor.pixel.harvest.dictionary

open class Dictionary {

    open val list: Set<String> = emptySet()

    fun getRandomUniqueItems(count: Int): List<String> {
        require(count <= list.size) { "Requested count exceeds the available unique items." }

        return list.shuffled().distinct().take(count)
    }

    open fun getEntry(item: String): String? {
        return list.find { it.equals(item, ignoreCase = true) }
    }

}