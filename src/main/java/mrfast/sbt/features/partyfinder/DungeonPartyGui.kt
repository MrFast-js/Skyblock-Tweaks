package mrfast.sbt.features.partyfinder

import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UMatrixStack
import kotlinx.coroutines.*
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.managers.FontManager
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.PartyManager
import mrfast.sbt.utils.*
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.LevelingUtils.roundToTwoDecimalPlaces
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.toFormattedDuration
import mrfast.sbt.utils.Utils.toTitleCase
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

class DungeonPartyGui : WindowScreen(ElementaVersion.V2) {
    private var guiTop = 0F
    private var guiLeft = 0F
    private val menuTexture = ResourceLocation("skyblocktweaks", "gui/dungeonParty.png")
    private val joinDungeonButton = GuiUtils.Button(82F, 19F, 8, 214)
    private val transferPartyButton = GuiUtils.Button(82F, 19F, 153, 214)
    private val kickPlayerButton = GuiUtils.Button(82F, 19F, 299, 214)
    private var selectedPlayer = ""
    private var selectedFloor = LocationManager.selectedDungeonFloor

    private var partyMemberApiData = mutableMapOf<String, JsonObject>()
    private val partyMemberProfileIds = mutableMapOf<String, String>()
    private val checkedPlayers = mutableListOf<String>()

    private var mouseX = 0.0
    private var mouseY = 0.0

    override fun onScreenClose() {
        super.onScreenClose()
        partyJob?.cancel()
    }

    init {
        addPartyPlayers()
    }

    private var partyJob: Job? = null
    private fun addPartyPlayers() {
        partyJob?.cancel()
        partyJob = CoroutineScope(Dispatchers.IO).launch {
            if (PartyManager.partyMembers.isNotEmpty() &&
                (selectedPlayer == "" || !partyMemberApiData.containsKey(selectedPlayer))
            ) {
                selectedPlayer = PartyManager.partyMembers.keys.find { it != Utils.getPlayer()!!.name } ?: ""
            }

            for ((name, _) in PartyManager.partyMembers) {
                if (name == Utils.getPlayer()!!.name || checkedPlayers.contains(name)) continue

                checkedPlayers.add(name)
                getApiData(name)

                if (NetworkUtils.tempApiAuthKey == "") {
                    delay(500)
                }
            }

            partyMemberApiData = partyMemberApiData
                .filterKeys { PartyManager.partyMembers.containsKey(it) }
                .toMutableMap()

            delay(200)

            if (PartyManager.partyMembers.size <= 1) {
                ChatUtils.sendClientMessage(
                    "§cYou must be in a party with players to use the Dungeon Party Gui!",
                    prefix = true
                )
                GuiUtils.closeGui()
            }
        }
    }

    private var lastMouseDown = false
    private var mouseClicking = false
    private var mouseDown = false

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)

        if (PartyManager.partyMembers.size <= 1) {
            ChatUtils.sendClientMessage(
                "§cYou must be in a party with players to use the Dungeon Party Gui!",
                prefix = true
            )
            GuiUtils.closeGui()
        }

        val res = ScaledResolution(Utils.mc)

        this.mouseX = mouseX.toDouble()
        this.mouseY = mouseY.toDouble()

        val mainBoxWidth = 391F
        val mainBoxHeight = 240F

        mouseDown = Mouse.isButtonDown(0)
        mouseClicking = lastMouseDown && !mouseDown
        lastMouseDown = mouseDown

        guiLeft = (res.scaledWidth - mainBoxWidth) / 2
        guiTop = (res.scaledHeight - mainBoxHeight) / 2

        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(menuTexture);
        GlStateManager.color(1f, 1f, 1f)
        GuiUtils.drawTexture(
            guiLeft,
            guiTop,
            mainBoxWidth,
            mainBoxHeight,
            0F,
            mainBoxWidth / 575F,
            0F,
            mainBoxHeight / 240F,
            GL11.GL_NEAREST
        )
        GlStateManager.popMatrix()


        if (joinDungeonButton.isClicked(this.mouseX, this.mouseY, guiLeft, guiTop) && mouseClicking) {
            GuiUtils.closeGui()
            ChatUtils.sendClientCommand("/jd $selectedFloor")
        }
        if (transferPartyButton.isClicked(this.mouseX, this.mouseY, guiLeft, guiTop) && mouseClicking) {
            ChatUtils.sendPlayerMessage("/p transfer $selectedPlayer")
        }
        if (kickPlayerButton.isClicked(this.mouseX, this.mouseY, guiLeft, guiTop) && mouseClicking) {
            ChatUtils.sendPlayerMessage("/p kick $selectedPlayer")
            if (PartyManager.partyMembers[Utils.getPlayer()!!.name]?.leader == true) {
                selectedPlayer = ""
            }
        }

        // Start stop buttons
        if (partyMemberApiData.containsKey(selectedPlayer)) {
            drawTabs()
            drawButtonText()
            drawInventory()
            drawArmorEquipment()
            drawStats()
            drawBonusItems()
            drawPet()
        }
    }

    private fun getApiData(username: String) {
        runBlocking {
            val playerUuid = NetworkUtils.getUUID(username) ?: return@runBlocking
            val profileInfo = NetworkUtils.getActiveProfile(playerUuid) ?: return@runBlocking
            val playerProfileInfo = profileInfo.asJsonObject["members"].asJsonObject[playerUuid].asJsonObject
            partyMemberProfileIds[username] = profileInfo.get("profile_id").asString
            partyMemberApiData[username] = playerProfileInfo
        }
    }

    private fun resetData() {
        loadedInventoryData.clear()
        loadedQuiverData.clear()
        loadedArmorData.clear()
        loadedEquipmentData.clear()
        gettingData = false
    }

    private var loadedInventoryData = mutableListOf<ItemStack?>()
    private fun drawInventory() {
        val data = partyMemberApiData[selectedPlayer] ?: return
        if (loadedInventoryData.isEmpty()) {
            val inventoryBase64 = data["inventory"]?.asJsonObject?.get("inv_contents")?.asJsonObject?.get("data")?.asString
            loadedInventoryData = ItemUtils.decodeBase64Inventory(inventoryBase64)
        }
        var apiDisabled = true
        for (i in 0 until loadedInventoryData.size) {
            if (loadedInventoryData[i] != null) apiDisabled = false
        }
        if (apiDisabled) {
            val barrier = ItemStack(Item.getItemFromBlock(Blocks.barrier))
            barrier.setStackDisplayName("§cAPI Disabled")
            for (i in 0 until 36) {
                loadedInventoryData.add(barrier)
            }
        }

        val invData = loadedInventoryData

        val inventoryOriginX = 122
        val inventoryOriginY = 104
        val hotbarOriginX = 122
        val hotbarOriginY = 179

        GlStateManager.color(1f, 1f, 1f)

        // Hotbar
        for ((index, itemStack) in invData.withIndex().take(9)) {
            val stack = itemStack ?: continue
            val slotY = index / 9
            val slotX = index % 9
            val itemX = guiLeft + hotbarOriginX + 24 * slotX
            val itemY = guiTop + hotbarOriginY + 24 * slotY

            GlStateManager.pushMatrix()
            GuiUtils.renderItemStackOnScreen(
                stack,
                itemX,
                itemY,
                22f,
                22f
            )
            GlStateManager.popMatrix()
            if (mouseX > itemX && mouseX < itemX + 22f && mouseY > itemY && mouseY < itemY + 22f) {
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                    itemStack.getLore(),
                    mouseX.toInt(),
                    mouseY.toInt(),
                    Utils.getScaledResolution().scaledWidth,
                    Utils.getScaledResolution().scaledHeight,
                    -1,
                    FontManager.getFontRenderer()
                )
            }
        }
        // Other 3 rows of inventory
        for ((index, itemStack) in invData.withIndex().drop(9)) {
            val stack = itemStack ?: continue
            val slotY = (index / 9) - 1
            val slotX = index % 9
            val itemX = guiLeft + inventoryOriginX + 24 * slotX
            val itemY = guiTop + inventoryOriginY + 24 * slotY

            GlStateManager.pushMatrix()
            GuiUtils.renderItemStackOnScreen(
                stack,
                itemX,
                itemY,
                22f,
                22f
            )
            GlStateManager.popMatrix()
            if (mouseX > itemX && mouseX < itemX + 22f && mouseY > itemY && mouseY < itemY + 22f) {
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                    itemStack.getLore(),
                    mouseX.toInt(),
                    mouseY.toInt(),
                    Utils.getScaledResolution().scaledWidth,
                    Utils.getScaledResolution().scaledHeight,
                    -1,
                    FontManager.getFontRenderer()
                )
            }
        }
    }

    private var loadedArmorData = mutableListOf<ItemStack?>()
    private var loadedEquipmentData = mutableListOf<ItemStack?>()

    private fun drawArmorEquipment() {
        val data = partyMemberApiData[selectedPlayer] ?: return
        if (loadedArmorData.isEmpty()) {
            val invArmorBase64 = data["inventory"]?.asJsonObject?.get("inv_armor")?.asJsonObject?.get("data")?.asString
            loadedArmorData = ItemUtils.decodeBase64Inventory(invArmorBase64)

            val equipBase64 = data["inventory"]?.asJsonObject?.get("equipment_contents")?.asJsonObject?.get("data")?.asString
            loadedEquipmentData = ItemUtils.decodeBase64Inventory(equipBase64)
        }
        var apiDisabled = true
        for (i in 0 until loadedArmorData.size) {
            if (loadedArmorData[i] != null || loadedEquipmentData[i] != null) apiDisabled = false
        }

        if (apiDisabled) {
            val barrier = ItemStack(Item.getItemFromBlock(Blocks.barrier))
            barrier.setStackDisplayName("§cAPI Disabled")
            for (i in 0 until 4) {
                loadedEquipmentData.add(barrier)
                loadedArmorData.add(barrier)
            }
        }

        val armorData = loadedArmorData
        val equipmentData = loadedEquipmentData

        val armorOriginX = 25
        val armorOriginY = 19

        GlStateManager.color(1f, 1f, 1f)

        // Armor
        for ((index, itemStack) in armorData.reversed().withIndex()) {
            val stack = itemStack ?: continue
            val slotY = index
            val itemX = guiLeft + armorOriginX
            val itemY = guiTop + armorOriginY + 24 * slotY

            GlStateManager.pushMatrix()
            GuiUtils.renderItemStackOnScreen(
                stack,
                itemX,
                itemY,
                22f,
                22f
            )
            GlStateManager.popMatrix()
            if (mouseX > itemX && mouseX < itemX + 22f && mouseY > itemY && mouseY < itemY + 22f) {
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                    itemStack.getLore(),
                    mouseX.toInt(),
                    mouseY.toInt(),
                    Utils.getScaledResolution().scaledWidth,
                    Utils.getScaledResolution().scaledHeight,
                    -1,
                    FontManager.getFontRenderer()
                )
            }
        }

        // Equipment
        for ((index, itemStack) in equipmentData.withIndex()) {
            val stack = itemStack ?: continue
            val slotY = index
            val itemX = guiLeft + armorOriginX + 24
            val itemY = guiTop + armorOriginY + 24 * slotY

            GlStateManager.pushMatrix()
            GuiUtils.renderItemStackOnScreen(
                stack,
                itemX,
                itemY,
                22f,
                22f
            )
            GlStateManager.popMatrix()
            if (mouseX > itemX && mouseX < itemX + 22f && mouseY > itemY && mouseY < itemY + 22f) {
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                    itemStack.getLore(),
                    mouseX.toInt(),
                    mouseY.toInt(),
                    Utils.getScaledResolution().scaledWidth,
                    Utils.getScaledResolution().scaledHeight,
                    -1,
                    FontManager.getFontRenderer()
                )
            }
        }
    }

    private var skycryptMemberData = mutableMapOf<String, JsonObject>()
    private var gettingData = false

    private fun drawPet() {
        if (!skycryptMemberData.containsKey(selectedPlayer) && !gettingData) {
            gettingData = true
            runBlocking {
                val skycryptProfiles =
                    NetworkUtils.apiRequestAndParse("https://sky.shiiyu.moe/api/v2/profile/$selectedPlayer")
                        .getAsJsonObject("profiles") ?: return@runBlocking
                val data = skycryptProfiles[partyMemberProfileIds[selectedPlayer]].asJsonObject["data"].asJsonObject
                    ?: return@runBlocking
                skycryptMemberData[selectedPlayer] = data
            }
        }
        // Pet Data - In order to get lore accurate pet, use skycrypt API
        val data = skycryptMemberData[selectedPlayer] ?: return
        val activePet = data["pets"].asJsonObject["pets"].asJsonArray.find {
            it.asJsonObject["active"].asBoolean
        }?.asJsonObject ?: return
        val customItemId = activePet["type"].asString + ";" + activePet["ref"].asJsonObject["rarity"].asString
        val petItem = ItemApi.createItemStack(customItemId) ?: return
        val petLevelName =
            "§7[Lvl ${activePet["ref"].asJsonObject["level"].asInt}] ${ItemUtils.rarityToColor(activePet["ref"].asJsonObject["rarity"].asInt)}${activePet["display_name"].asString}"
        petItem.setStackDisplayName(petLevelName)

        val petStackX = guiLeft + 37
        val petStackY = guiTop + 132

        GlStateManager.pushMatrix()
        GuiUtils.renderItemStackOnScreen(
            petItem,
            petStackX,
            petStackY,
            22f,
            22f
        )
        GlStateManager.popMatrix()
        if (mouseX > petStackX && mouseX < petStackX + 22f && mouseY > petStackY && mouseY < petStackY + 22f) {
            val lore = mutableListOf(petItem.displayName)
            activePet["tag"].asJsonObject["display"].asJsonObject["Lore"].asJsonArray.forEach {
                lore.add(it.asString)
            }
            net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                fixLongLore(lore),
                mouseX.toInt(),
                mouseY.toInt(),
                Utils.getScaledResolution().scaledWidth,
                Utils.getScaledResolution().scaledHeight,
                -1,
                FontManager.getFontRenderer()
            )
        }
    }

    private var loadedQuiverData = mutableMapOf<String, Int>()
    private fun drawBonusItems() {
        // Quiver
        val data = partyMemberApiData[selectedPlayer] ?: return
        if (loadedQuiverData.isEmpty()) {
            val inventoryBase64 = data["inventory"]?.asJsonObject?.get("bag_contents")?.asJsonObject?.get("quiver")?.asJsonObject?.get("data")?.asString
            ItemUtils.decodeBase64Inventory(inventoryBase64).forEach {
                val arrow = it?.displayName ?: return
                if (!loadedQuiverData.containsKey(arrow)) {
                    loadedQuiverData[arrow] = 0
                }
                loadedQuiverData[arrow] = loadedQuiverData[arrow]!! + it.stackSize
            }
        }
        val topArrow =
            if (loadedQuiverData.keys.isNotEmpty()) loadedQuiverData.keys.sortedByDescending { loadedQuiverData[it] }[0] else "§fFlint Arrow"
        var totalArrows = 0;loadedQuiverData.values.forEach { totalArrows += it }

        val quiverStackX = guiLeft + 37
        val quiverStackY = guiTop + 156
        GlStateManager.pushMatrix()
        GuiUtils.renderItemStackOnScreen(
            ItemApi.createItemStack(ItemApi.getItemIdFromName(topArrow)!!),
            quiverStackX,
            quiverStackY,
            22f,
            22f
        )
        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 200f)
        fontRendererObj.drawString(
            totalArrows.abbreviateNumber(),
            quiverStackX + 25f - fontRendererObj.getStringWidth(totalArrows.abbreviateNumber()),
            quiverStackY + 15f,
            0xFFFFFF,
            true
        )
        GlStateManager.translate(0f, 0f, -200f)
        GlStateManager.popMatrix()

        if (mouseX > quiverStackX && mouseX < quiverStackX + 22f && mouseY > quiverStackY && mouseY < quiverStackY + 22f) {
            val lore = mutableListOf<String>()
            loadedQuiverData.forEach { (key, value) ->
                lore.add("§8x$value $key")
            }
            net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                lore.reversed(),
                mouseX.toInt(),
                mouseY.toInt(),
                Utils.getScaledResolution().scaledWidth,
                Utils.getScaledResolution().scaledHeight,
                -1,
                FontManager.getFontRenderer()
            )
        }

        val magicPower = data.getAsJsonObject("accessory_bag_storage")
            ?.get("highest_magical_power")
            ?.asInt ?: -1

        val magicPowerStackX = guiLeft + 37
        val magicPowerStackY = guiTop + 180
        GlStateManager.pushMatrix()
        GuiUtils.renderItemStackOnScreen(
            ItemApi.createItemStack("ACCESSORY_BAG"),
            magicPowerStackX,
            magicPowerStackY,
            22f,
            22f
        )
        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 200f)
        fontRendererObj.drawString(
            magicPower.abbreviateNumber(),
            magicPowerStackX + 25f - fontRendererObj.getStringWidth(magicPower.abbreviateNumber()),
            magicPowerStackY + 15f,
            0xFFFFFF,
            true
        )
        GlStateManager.translate(0f, 0f, -200f)
        GlStateManager.popMatrix()
    }

    private fun drawTabs() {
        var i = 0
        partyMemberApiData.keys.sorted().forEach { username ->
            val tabX = guiLeft + 10 + 93f * i
            val tabY: Float

            // Draw elevated (selected) tab texture
            if (selectedPlayer == username) {
                GlStateManager.pushMatrix()
                Minecraft.getMinecraft().textureManager.bindTexture(menuTexture);
                GlStateManager.color(1f, 1f, 1f)
                tabY = guiTop - 36 + 4
                GuiUtils.drawTexture(tabX, tabY, 91f, 36f, 392f / 575F, 483f / 575F, 0F, 36f / 240F, GL11.GL_NEAREST)
                GlStateManager.popMatrix()
            } else {
                tabY = guiTop - 29
                GlStateManager.pushMatrix()
                Minecraft.getMinecraft().textureManager.bindTexture(menuTexture);
                GlStateManager.color(1f, 1f, 1f)
                GuiUtils.drawTexture(
                    tabX,
                    tabY,
                    91f,
                    29f,
                    484 / 575F,
                    575f / 575F,
                    3F / 240F,
                    32f / 240F,
                    GL11.GL_NEAREST
                )
                GlStateManager.popMatrix()
            }

            GuiUtils.drawText("§a$username", tabX + 4, tabY + 4, GuiUtils.TextStyle.BLACK_OUTLINE, Color.CYAN)

            // For future implementation
//            val onlineStatusColor = if (PartyManager.partyMembers[username]!!.online) "§a" else "§c"
            val data = partyMemberApiData[username]?:return
            val dungeonsData = if(data.has("dungeons")) data["dungeons"].asJsonObject else JsonObject()
            val selectedClass = if(dungeonsData.has("selected_dungeon_class")) dungeonsData["selected_dungeon_class"].asString else "NO API" // Default to archer
            val classLvl = if(dungeonsData.has("player_classes")) {
                if(dungeonsData["player_classes"].asJsonObject.has(selectedClass)) {
                    LevelingUtils.calculateDungeonsLevel(dungeonsData["player_classes"].asJsonObject[selectedClass].asJsonObject["experience"].asDouble).toInt()
                } else 0
            } else 0
            val leaderText = if (PartyManager.partyMembers[username]?.leader == true) " §6♚" else ""

            GuiUtils.drawText(
                "§7Lvl $classLvl ${getClassColor(selectedClass)}${selectedClass.toTitleCase()}$leaderText",
                tabX + 4,
                tabY + 15,
                GuiUtils.TextStyle.BLACK_OUTLINE,
                Color.CYAN
            )

            if (mouseX > tabX && mouseX < tabX + 91f && mouseY > tabY && mouseY < tabY + 29f) {
                GuiUtils.drawText(
                    "[X]",
                    tabX + 91f - 13f,
                    tabY + 2,
                    GuiUtils.TextStyle.BLACK_OUTLINE,
                    Color.RED
                )

                if (mouseClicking) {
                    // Kick button [X]
                    if (mouseX > tabX + 91f - 13f && mouseX < tabX + 91f && mouseY > tabY && mouseY < tabY + 10) {
                        if (PartyManager.partyMembers[Utils.getPlayer()!!.name]?.leader == true) {
                            ChatUtils.sendPlayerMessage("/p kick $username")
                            if (PartyManager.partyMembers[Utils.getPlayer()!!.name]?.leader == true) {
                                selectedPlayer = ""
                            }
                            return
                        }
                    }
                    // Stop from clicking on already selected tab
                    if (selectedPlayer == username) return
                    selectedPlayer = username
                    resetData()
                }
            }
            i++
        }
    }

    private fun drawStats() {
        val data = partyMemberApiData[selectedPlayer]

        GlStateManager.translate(guiLeft + 115f, guiTop + 18f, 0f)
        if(data == null) {
            fontRendererObj.drawString("§c§lCould not get API data for player", 0f, -12f, 0xFFFFFF, true)
            GlStateManager.translate(-(guiLeft + 115f), -(guiTop + 18f), 0f)
            return
        }
        if(!data.has("dungeons")) {
            fontRendererObj.drawString("§c§lNo dungeon data. Player's first run?", 0f, -12f, 0xFFFFFF, true)
            fontRendererObj.drawString("§c§lor has API disabled", 0f, -1f, 0xFFFFFF, true)
            GlStateManager.translate(-(guiLeft + 115f), -(guiTop + 18f), 0f)
            return
        }

        val dungeonsData = data["dungeons"].asJsonObject
        val selectedClass = dungeonsData["selected_dungeon_class"]?.asString ?: "UNKNOWN"
        val classLvl = LevelingUtils.calculateDungeonsLevel(dungeonsData["player_classes"].asJsonObject[selectedClass].asJsonObject["experience"].asDouble)
        val secrets = data.getAsJsonObject("dungeons")
            ?.get("secrets")
            ?.asInt ?: -1
        val dungeonType = dungeonsData["dungeon_types"].asJsonObject["catacombs"]
        val floorCompletions = dungeonType.asJsonObject["tier_completions"]?.asJsonObject?.get(selectedFloor) ?: 0
        val totalFloorCompletions = dungeonType.asJsonObject["tier_completions"]?.asJsonObject?.entrySet()
            ?.sumOf { it.value.asInt } ?: 0

        val cataXp = dungeonsData["dungeon_types"].asJsonObject["catacombs"].asJsonObject["experience"]?.asDouble ?: 0.0
        val cataLvl = LevelingUtils.calculateDungeonsLevel(cataXp)
        val catacombsWatcherKills = data["player_stats"].getAsJsonObject()["kills"].asJsonObject["watcher_summon_undead"]?.asInt ?: 0

        fontRendererObj.drawString("§e§lPlayer Stats", 0f, 0f, 0xFFFFFF, true)
        fontRendererObj.drawString("§6Catacombs: $cataLvl", 0f, 9f, 0xFFFFFF, true)
        fontRendererObj.drawString(
            "${getClassColor(selectedClass)}${selectedClass.toTitleCase()}: $classLvl",
            0f,
            18f,
            0xFFFFFF,
            true
        )
        fontRendererObj.drawString("§7Secrets: ${secrets.formatNumber()}", 0f, 27f, 0xFFFFFF, true)
        fontRendererObj.drawString(
            "§cWatcher Kills: ${(catacombsWatcherKills).formatNumber()}",
            0f,
            36f,
            0xFFFFFF,
            true
        )
        fontRendererObj.drawString(
            "§bSecrets/Run: ${(secrets.toDouble()/totalFloorCompletions).roundToTwoDecimalPlaces()}",
            0f,
            45f,
            0xFFFFFF,
            true
        )
        GlStateManager.translate(-(guiLeft + 115f), -(guiTop + 18f), 0f)

        val floorBestScore = dungeonType.asJsonObject["best_score"]?.asJsonObject?.get(selectedFloor) ?: 0
        val floorBestTime = dungeonType.asJsonObject["fastest_time"]?.asJsonObject?.get(selectedFloor)?.asLong ?: -1L
        val floorBestSTime = dungeonType.asJsonObject["fastest_time_s"]?.asJsonObject?.get(selectedFloor)?.asLong ?: -1L
        val floorBestSPTime =
            dungeonType.asJsonObject["fastest_time_s_plus"]?.asJsonObject?.get(selectedFloor)?.asLong ?: -1

        GlStateManager.translate(guiLeft + 242f, guiTop + 18f, 0f)
        fontRendererObj.drawString("§e§lFloor $selectedFloor Stats", 0f, 0f, 0xFFFFFF, true)
        fontRendererObj.drawString("§9Completions: $floorCompletions", 0f, 9f, 0xFFFFFF, true)
        fontRendererObj.drawString("§bBest Score: $floorBestScore", 0f, 18f, 0xFFFFFF, true)
        fontRendererObj.drawString(
            "§3Fastest Run: ${floorBestTime.toFormattedDuration()}",
            0f,
            27f,
            0xFFFFFF,
            true
        )
        fontRendererObj.drawString("§aFastest S: ${floorBestSTime.toFormattedDuration()}", 0f, 36f, 0xFFFFFF, true)
        fontRendererObj.drawString(
            "§6Fastest S+: ${floorBestSPTime.toFormattedDuration()}",
            0f,
            45f,
            0xFFFFFF,
            true
        )
        GlStateManager.translate(-(guiLeft + 242f), -(guiTop + 18f), 0f)
    }

    private fun drawButtonText() {
        fontRendererObj.drawString("§aJoin Dungeon", (guiLeft + 11 + 5).toInt(), (guiTop + 217 + 3).toInt(), 0xFFFFFF)
        fontRendererObj.drawString("§9Transfer Party", (guiLeft + 156).toInt(), (guiTop + 217 + 3).toInt(), 0xFFFFFF)
        fontRendererObj.drawString("§cKick Player", (guiLeft + 302 + 11).toInt(), (guiTop + 217 + 3).toInt(), 0xFFFFFF)
    }

    private fun getClassColor(className: String): String {
        when (className) {
            "archer" -> return "§6"
            "berserk" -> return "§4"
            "healer" -> return "§d"
            "mage" -> return "§b"
            "tank" -> return "§2"
        }
//        "archer" -> return "§2"
//        "berserk" -> return "§c"
//        "healer" -> return "§d"
//        "mage" -> return "§b"
//        "tank" -> return "§f"
        return "§4"
    }

    private fun fixLongLore(lore: List<String>): MutableList<String> {
        val wrappedTexts = mutableListOf<String>()
        val colorCodeRegex = """§[0-9a-fk-or]""".toRegex()

        lore.forEach { text ->
            var currentText = text
            var currentColor = colorCodeRegex.findAll(text).lastOrNull()?.value ?: ""

            while (currentText.clean().length > 38) {
                val breakIndex = currentText.lastIndexOf(' ', 38).takeIf { it != -1 } ?: 38
                val line = currentText.substring(0, breakIndex).trimEnd()
                wrappedTexts.add(line)
                currentText = currentText.substring(breakIndex).trimStart()
                currentColor = colorCodeRegex.findAll(line).lastOrNull()?.value ?: currentColor
                if (!currentText.startsWith(currentColor)) currentText = currentColor + currentText
            }
            wrappedTexts.add(currentText)
        }
        return wrappedTexts
    }
}