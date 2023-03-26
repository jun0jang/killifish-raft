package jraft.common.config

class ConfigDef(
    private val configKeys: MutableMap<String, ConfigKey> = mutableMapOf(),
    private val groups: MutableList<String> = mutableListOf(),
) {
    fun define(configKey: ConfigKey): ConfigDef {
        configKeys[configKey.name] = configKey
        return this
    }

    data class ConfigKey(
        val name: String,
        val type: Type,
        val documentation: String = "",
        val defaultValue: Any? = null,
    )

    enum class Type {
        BOOLEAN,
        STRING,
    }
}
