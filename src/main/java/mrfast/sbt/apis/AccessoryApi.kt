package mrfast.sbt.apis

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.getInventory
import mrfast.sbt.utils.Utils.matches
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object AccessoryApi {
    private var ignoredAccessories = mutableSetOf<String>() // Talismans that the accessory bag doesnt count for MP
    private val allAccessoriesJson = DataManager.getDataDefault("AllAccessories", JsonObject()) as JsonObject
    private var playerAccessoryBagJson = JsonArray()
    var missing = JsonArray()
    var missingMax = JsonArray()

    @SubscribeEvent
    fun onProfileLoad(event: ProfileLoadEvent) {
        playerAccessoryBagJson = DataManager.getProfileDataDefault("accessory_bag", JsonArray()) as JsonArray
        updateMissing()
        loadAllAccessories()
    }

    private fun loadTalismanUpgrades() {
        val misc = NetworkUtils.NeuConstants.get("misc").asJsonObject

        // Load talisman upgrades
        val upgradesJson = misc.getAsJsonObject("talisman_upgrades")
        for ((baseKey, value) in upgradesJson.entrySet()) {
            val upgradeArray = value.asJsonArray

            val combined = JsonArray()
            combined.add(JsonPrimitive(baseKey))
            upgradeArray.forEach { combined.add(JsonPrimitive(it.asString)) }

            allAccessoriesJson.add(baseKey, combined)
        }
    }

    private fun registerAccessories() {
        ItemApi.getSkyblockItems().entrySet().forEach { item ->
            val lore = item.value.asJsonObject?.get("lore")?.asJsonArray ?: return@forEach

            if(lore.last().asString.contains("ACCESSORY")) {
                val combined = JsonArray()
                combined.add(JsonPrimitive(item.key))

                // Ignore accessories that are already in the list somewhere
                if (allAccessoriesJson.entrySet().any { it.value.asJsonArray.any { it2 -> it2.asString == item.key } }) {
                    return@forEach
                }

                if (item.key.contains("_HAT_")) return

                allAccessoriesJson.add(item.key, combined)
            }
        }
    }

    private fun loadAllAccessories() {
        // Register all talisman upgrades
        loadTalismanUpgrades()

        // Register all non existing accessories
        registerAccessories()

        // Remove base keys that are included in another upgrade chain
        val keysToRemove = mutableSetOf<String>()
        for ((key, list) in allAccessoriesJson.entrySet()) {
            for ((otherKey, otherList) in allAccessoriesJson.entrySet()) {
                if (otherKey != key && otherList.asJsonArray.map { it.asString }.contains(key)) {
                    keysToRemove.add(key)
                    break
                }
            }
        }
        keysToRemove.forEach { allAccessoriesJson.remove(it) }

        DataManager.saveData("accessories", allAccessoriesJson)

        NetworkUtils.NeuConstants.get("misc").asJsonObject.get("ignored_talisman").asJsonArray.forEach {ignoredAccessories.add(it.asString)}
    }

    fun getMagicPowerValue(id: String): Int {
        val item = ItemApi.getItemInfo(id) ?: return -2
        val rarityElement = item.asJsonObject?.get("rarity")
        val rarity = if (rarityElement != null && !rarityElement.isJsonNull) rarityElement.asString else return -1

        when (rarity) {
            "COMMON" -> return 3
            "SPECIAL" -> return 3
            "VERY_SPECIAL" -> return 5
            "UNCOMMON" -> return 5
            "RARE" -> return 8
            "EPIC" -> return 12
            "LEGENDARY" -> return 16
            "MYTHIC" -> return 22
            "DIVINE" -> return 30
        }
        return 0
    }

    private val ACCESSORY_BAG_REGEX = """Accessory Bag(?: \(.*\/.*\))?$""".toRegex()
    fun isAccessoryBagOpen(): Boolean {
        val gui = Utils.getCurrentScreen()
        if (gui is GuiChest) {
            val chestName = gui.chestName()
            return chestName.matches(ACCESSORY_BAG_REGEX)
        }
        return false
    }

    @SubscribeEvent
    fun onGuiDraw(event: GuiContainerBackgroundDrawnEvent) {
        if (event.gui is GuiChest) {
            if(!isAccessoryBagOpen()) return

            val inv = (event.gui as GuiChest).getInventory()

            for (i in 0 until (event.gui as GuiChest).getInventory().sizeInventory) {
                val stack = inv.getStackInSlot(i) ?: continue
                val id = stack.getSkyblockId() ?: continue

                if(!playerAccessoryBagJson.contains(JsonPrimitive(id))) {
                    playerAccessoryBagJson.add(JsonPrimitive(id))
                }
            }

            DataManager.saveProfileData("accessory_bag", playerAccessoryBagJson)

            updateMissing()
        }
    }

    private fun updateMissing() {
        missing = JsonArray()
        missingMax = JsonArray()

        // Check if accessory is a base level
        for ((baseLevel, levels) in allAccessoriesJson.entrySet()) {
            // Check if player has a base level
            if (ignoredAccessories.contains(baseLevel) || missing.contains(JsonPrimitive(baseLevel))) continue

            // Check if player has an upgrade of that base item
            val upgrades = levels?.asJsonArray ?: continue

            var foundUpgrade = false
            for (upgrade in upgrades) {
                if(foundUpgrade && !missing.contains(upgrade)) {
                    // Add levels to max
                    missing.add(upgrade)
                }

                // Check if player has a tier of that upgrade
                if (playerAccessoryBagJson.contains(upgrade)) {
                    foundUpgrade = true
                }
            }

            val maxTier = upgrades.lastOrNull()
            if(maxTier != null && !missingMax.contains(maxTier) && !playerAccessoryBagJson.contains(maxTier)) {
                // Add levels to max
                missingMax.add(maxTier)
            }

            if (!foundUpgrade) {
                // Add base tiers
                missing.add(JsonPrimitive(baseLevel))
            }
        }
    }
}