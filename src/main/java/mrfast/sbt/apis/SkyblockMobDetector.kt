package mrfast.sbt.apis

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.config.categories.DeveloperConfig.showMobIdsThroughWalls
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

@SkyblockTweaks.EventComponent
object SkyblockMobDetector {
    private val skyblockMobHashMap = HashMap<Entity, SkyblockMob>()

    class SkyblockMob(val mobNameEntity: Entity, val skyblockMob: Entity) {
        var skyblockMobId: String? = null
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        skyblockMobHashMap.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (Utils.mc.theWorld == null || !LocationManager.inSkyblock) return

        if(TickManager.tickCount % 4 != 0) return

        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (Utils.mc.thePlayer.getDistanceToEntity(entity) > 30) continue

            val potentialMob = when {
                entity.customNameTag?.contains("Withermancer") == true -> Utils.mc.theWorld.getEntityByID(entity.entityId - 3)
                else -> Utils.mc.theWorld.getEntityByID(entity.entityId - 1)
            } ?: continue

            if (entity is EntityArmorStand && entity.hasCustomName() && !skyblockMobHashMap.containsKey(entity)) {
                if (potentialMob.isEntityAlive && potentialMob !is EntityArrow) {
                    val sbMob = SkyblockMob(entity, potentialMob)
                    skyblockMobHashMap[entity] = sbMob
                }
            }
        }

        for (sbMob in skyblockMobHashMap.values) {
            if (Utils.mc.thePlayer.getDistanceToEntity(sbMob.skyblockMob) > 30) continue

            if (sbMob.skyblockMobId == null) {
                updateMobData(sbMob)
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        Utils.setTimeout({
            val mob = getSkyblockMob(event.entity) ?: return@setTimeout
            MinecraftForge.EVENT_BUS.post(SkyblockMobEvent.Spawn(mob,mob.skyblockMob.positionVector))
        }, 300)
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
        val flag1 =
            showMobIdsThroughWalls || (Utils.mc.thePlayer.canEntityBeSeen(sbMob.skyblockMob) && !showMobIdsThroughWalls)
        if (sbMob.skyblockMobId != null && CustomizationConfig.developerMode && flag1 && show) {
            val pos = Vec3(sbMob.skyblockMob.posX, sbMob.skyblockMob.posY + 1, sbMob.skyblockMob.posZ)

            GlStateManager.disableDepth()
            RenderUtils.draw3DString("§e${sbMob.skyblockMobId}", pos, event.partialTicks!!)
            GlStateManager.enableDepth()
        }
    }

    private fun updateMobData(sbMob: SkyblockMob) {
        val rawMobName = sbMob.mobNameEntity.displayName.unformattedText.clean().replace(",", "")

        val normalMobRegex = "\\[Lv(?:\\d+k?)] (.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤"
        val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?"
        val dungeonMobRegex = "✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤"

        var pattern: Pattern
        var matcher: Matcher? = null

        // Iterate through the regex patterns
        val regexPatterns = listOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)
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

    fun getLoadedSkyblockMobs(): List<SkyblockMob> = ArrayList(skyblockMobHashMap.values)

    fun getEntityByName(id: String): Entity? = getLoadedSkyblockMobs()
        .firstOrNull { mob -> mob.skyblockMobId == id }
        ?.skyblockMob

    fun getEntitiesByName(id: String): List<Entity> = getLoadedSkyblockMobs()
        .filter { mob -> mob.skyblockMobId == id }
        .map { mob -> mob.skyblockMob }

    fun getSkyblockMob(entity: Entity): SkyblockMob? = getLoadedSkyblockMobs()
        .firstOrNull { mob -> mob.skyblockMob == entity || mob.mobNameEntity == entity }

    fun getEntityId(entity: Entity): String? = getLoadedSkyblockMobs()
        .firstOrNull { mob -> mob.skyblockMob == entity }
        ?.skyblockMobId
}