package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType

object HiddenConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Sent Trade History Hint",
        description = "",
        category = "§3Hidden",
        subcategory = ""
    )
    var sentTradeHistoryPrompt = false
}