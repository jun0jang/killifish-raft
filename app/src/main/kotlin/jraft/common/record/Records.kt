package jraft.common.record

interface Records {
    companion object {
        const val OFFSET_OFFSET: Int = 0
        const val OFFSET_LENGTH: Int = 8
        const val SIZE_OFFSET: Int = OFFSET_OFFSET + OFFSET_LENGTH
        const val SIZE_LENGTH: Int = 4
        const val LOG_OVERHEAD: Int = SIZE_OFFSET + SIZE_LENGTH
    }
}
