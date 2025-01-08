package mrfast.sbt.features.general

import com.google.gson.JsonArray
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object ItemPriceDescription {

    @SubscribeEvent
    fun onToolTip(event: ItemTooltipEvent) {
        if(!MiscellaneousConfig.showItemPricingData) return

        val stack = event.itemStack
        val pricingData = ItemApi.getItemInfo(stack) ?: return

        pricingData.takeIf { it.has("activeBin") || it.has("activeAuc") }?.let {
            val activeBinNum = if (it.has("activeBin")) it.get("activeBin").asInt else -1
            val activeAucNum = if (it.has("activeAuc")) it.get("activeAuc").asInt else -1
            val activeBin = if (activeBinNum != -1) activeBinNum.formatNumber() else "§4Unknown" // Red for missing
            val activeAuc = if (activeAucNum != -1) activeAucNum.formatNumber() else "§4Unknown"
            event.toolTip.add("§3Active: §a$activeBin BIN §7| §b$activeAuc AUC")
        }

        pricingData.takeIf { it.has("binSold") || it.has("aucSold") }?.let {
            val soldBinNum = if (it.has("binSold")) it.get("binSold").asInt else -1
            val soldAucNum = if (it.has("aucSold")) it.get("aucSold").asInt else -1
            val soldBin = if (soldBinNum != -1) soldBinNum.formatNumber() else "§4Unknown"
            val soldAuc = if (soldAucNum != -1) soldAucNum.formatNumber() else "§4Unknown"
            event.toolTip.add("§3Sales/day: §a$soldBin BIN §7| §b$soldAuc AUC")
        }

        pricingData.takeIf { it.has("avgLowestBin") && it.has("lowestBin") }?.let {
            val avgLowestBin = it.get("avgLowestBin").asLong.formatNumber()
            val lowestBin = it.get("lowestBin").asLong.formatNumber()
            event.toolTip.add("§3BIN: §7AVG §e$avgLowestBin §8| §7LOW §e$lowestBin")
        }

        pricingData.takeIf { it.has("avgAucPrice") && it.has("aucPrice") }?.let {
            val avgAucPrice = it.get("avgAucPrice").asLong.formatNumber()
            val aucPrice = it.get("aucPrice").asLong.formatNumber()
            event.toolTip.add("§3AUC: §7AVG §e$avgAucPrice §8| §7SOON §e$aucPrice")
        }

        pricingData.takeIf { it.has("bazaarBuy") || it.has("bazaarSell") }?.let {
            val bazaarBuy = it.takeIf { it.has("bazaarBuy") }?.get("bazaarBuy")?.asLong?.times(stack.stackSize)?.formatNumber() ?: "§4Unknown"
            val bazaarSell = it.takeIf { it.has("bazaarSell") }?.get("bazaarSell")?.asLong?.times(stack.stackSize)?.formatNumber() ?: "§4Unknown"
            event.toolTip.add("§3Bazaar: §a$bazaarBuy Buy §7| §b$bazaarSell Sell")
        }
    }

}