package mrfast.sbt.apis

import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

object SkyblockMobDetector {
    private val skyblockMobHashMap = HashMap<Entity, SkyblockMob>()

    class SkyblockMob(val mobNameEntity: Entity, val skyblockMob: Entity) {
        var skyblockMobId: String? = null
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        skyblockMobHashMap.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (Utils.mc.theWorld == null || !LocationUtils.inSkyblock) return

        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (entity is EntityArmorStand && !skyblockMobHashMap.containsKey(entity) && entity.hasCustomName()) {
                if (Utils.mc.thePlayer.getDistanceToEntity(entity) > 30) continue
                val potentialMob = Utils.mc.theWorld.getEntityByID(entity.entityId - 1)
                    ?: Utils.mc.theWorld.getEntityByID(entity.entityId - 3)
                if (potentialMob == null || !potentialMob.isEntityAlive) continue

                val sbMob = SkyblockMob(entity, potentialMob)
                skyblockMobHashMap[entity] = sbMob
            }
        }
        for (sbMob in skyblockMobHashMap.values) {
            if (Utils.mc.thePlayer.getDistanceToEntity(sbMob.skyblockMob) > 30) continue

            if (sbMob.skyblockMobId == null) {
                updateMobData(sbMob)
                if (sbMob.skyblockMobId != null) {
                    MinecraftForge.EVENT_BUS.post(SkyblockMobEvent.Spawn(sbMob))
                }
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val iterator = skyblockMobHashMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val sbMob = entry.value

            if (!sbMob.skyblockMob.isEntityAlive) {
                iterator.remove()
                if (sbMob.skyblockMobId != null) {
                    if (Utils.mc.thePlayer.getDistanceToEntity(sbMob.mobNameEntity) > 30) continue

                    MinecraftForge.EVENT_BUS.post(SkyblockMobEvent.Death(sbMob))
                }
            } else {
                MinecraftForge.EVENT_BUS.post(SkyblockMobEvent.Render(sbMob, event.partialTicks))
            }
        }
    }

    @SubscribeEvent
    fun onRenderMob(event: SkyblockMobEvent.Render) {
        val sbMob = event.sbMob
        val show = CustomizationConfig.developerMode && DeveloperConfig.showMobIds
        if (sbMob.skyblockMobId != null && CustomizationConfig.developerMode && Utils.mc.thePlayer.canEntityBeSeen(sbMob.skyblockMob) && show) {
            val pos = Vec3(sbMob.skyblockMob.posX, sbMob.skyblockMob.posY + 1, sbMob.skyblockMob.posZ)
            GlStateManager.disableDepth()
            RenderUtils.draw3DString("§e${sbMob.skyblockMobId}", pos, event.partialTicks!!)
            GlStateManager.enableDepth()
        }
    }

    private fun updateMobData(sbMob: SkyblockMob) {
        val rawMobName = sbMob.mobNameEntity.displayName.unformattedText.clean()

        val normalMobRegex = "\\[Lv(?:\\d+k?)] (.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤"
        val slayerMobRegex = "(?<=☠\\s)\\w+\\s\\w+\\s\\w+"
        val dungeonMobRegex =
            "✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤"

        var pattern: Pattern
        var matcher: Matcher? = null

        // Iterate through the regex patterns
        val regexPatterns = arrayOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)
        var regexBeingUsed: String? = null
        for (regex in regexPatterns) {
            pattern = Pattern.compile(regex)
            matcher = pattern.matcher(rawMobName)
            if (matcher.find()) {
                regexBeingUsed = regex
                break
            }
        }

        if (regexBeingUsed != null) {
            matcher = matcher ?: return
            when (regexBeingUsed) {
                normalMobRegex -> sbMob.skyblockMobId = matcher.group(1)
                slayerMobRegex -> sbMob.skyblockMobId = matcher.group() + " Slayer"
                dungeonMobRegex -> {
                    sbMob.skyblockMobId = matcher.group(1)
                    if (rawMobName.startsWith("ൠ")) {
                        sbMob.skyblockMobId = matcher.group(1) + " Pest"
                    }
                }
            }
            // Remove &k obfuscation on mobs that are corrupted
            sbMob.skyblockMobId?.let { skyblockMobId ->
                if (skyblockMobId.startsWith("a") && Character.isUpperCase(skyblockMobId[1])) {
                    sbMob.skyblockMobId = skyblockMobId.substring(1, skyblockMobId.length - 2)
                }
            }
        }
    }

    fun getLoadedSkyblockMobs(): List<SkyblockMob> {
        return ArrayList(skyblockMobHashMap.values)
    }

    fun getEntityByName(id: String): Entity? {
        return getLoadedSkyblockMobs()
            .firstOrNull { mob -> mob.skyblockMobId == id }
            ?.skyblockMob
    }

    fun getEntitiesByName(id: String): List<Entity> {
        return getLoadedSkyblockMobs()
            .filter { mob -> mob.skyblockMobId == id }
            .map { mob -> mob.skyblockMob }
    }

    fun getSkyblockMob(entity: Entity): SkyblockMob? {
        return getLoadedSkyblockMobs()
            .firstOrNull { mob -> mob.skyblockMob == entity || mob.mobNameEntity == entity }
    }

    fun getEntityId(entity: Entity): String? {
        return getLoadedSkyblockMobs()
            .firstOrNull { mob -> mob.skyblockMob == entity }
            ?.skyblockMobId
    }
}
