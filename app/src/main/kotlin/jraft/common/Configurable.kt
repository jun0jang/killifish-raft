package jraft.common

interface Configurable {
    fun configure(configs: Map<String, *>)
}
