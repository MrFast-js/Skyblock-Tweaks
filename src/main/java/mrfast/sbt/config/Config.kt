package mrfast.sbt.config

import mrfast.sbt.config.categories.*

// Import the ConfigProperty annotation if it's in a different package

open class Config : ConfigManager() {
    // Config can be put into here but should rather go into Categories
    // Use this to setup order for sidebar
    val list = listOf(
        GeneralConfig,
        //MiningConfig,
        //FishingConfig,
        //EventsConfig,
        //DungeonConfig,
        //AuctionFlipperConfig,
        //MiscellaneousConfig,
        //OverlaysConfig,
        //RiftConfig,
        //RenderingConfig,
        CustomizationConfig,
        DeveloperConfig
    )
}