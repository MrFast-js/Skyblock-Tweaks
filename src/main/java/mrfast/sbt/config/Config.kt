package mrfast.sbt.config

import mrfast.sbt.config.categories.*

open class Config : ConfigManager() {
    // Config can be put into here but should rather go into Categories
    // Use this to setup order for sidebar
    val list = listOf(
        GeneralConfig,
        //MiningConfig,
        //FishingConfig,
        //EventsConfig,
        DungeonConfig,
        AuctionHouseConfig,
        MiscellaneousConfig,
        CrimsonConfig,
        //OverlaysConfig,
        RiftConfig,
        RenderingConfig,
        CustomizationConfig,
        DeveloperConfig
    )
}