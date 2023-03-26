package jraft.clients.consumer

import jraft.common.config.AbstractConfig
import jraft.common.config.ConfigDef

class ConsumerConfig : AbstractConfig() {
    companion object {
        const val BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers"

        const val CLIENT_ID_CONFIG = "client.id"

        const val MAX_POLL_RECORDS_CONFIG = "max.poll.records"

        val CONFIG = ConfigDef().define(
            ConfigDef.ConfigKey(name = BOOTSTRAP_SERVERS_CONFIG, type = ConfigDef.Type.STRING),
        ).define(
            ConfigDef.ConfigKey(name = CLIENT_ID_CONFIG, type = ConfigDef.Type.STRING),
        ).define(
            ConfigDef.ConfigKey(name = MAX_POLL_RECORDS_CONFIG, type = ConfigDef.Type.STRING),
        )
    }
}
