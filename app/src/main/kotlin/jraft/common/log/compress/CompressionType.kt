package jraft.common.log.compress

enum class CompressionType(
    val id: Int,
) {
    NONE(id = 0),
    GZIP(id = 1),
    ;

    companion object {
        fun of(id: Int): CompressionType {
            return values().first { it.id == id }
        }
    }
}
