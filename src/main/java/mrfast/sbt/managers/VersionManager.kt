package mrfast.sbt.managers

import moe.nea.libautoupdate.*
import mrfast.sbt.SkyblockTweaks.Companion.MOD_ID
import mrfast.sbt.SkyblockTweaks.Companion.MOD_VERSION
import mrfast.sbt.config.Categories.CustomizationConfig
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.IOException
import java.util.concurrent.CompletableFuture

object VersionManager {
    var neededUpdate = NeededUpdate()
    private var potentialUpdate: PotentialUpdate? = null
    private var didSilentCheck = false
    private var context = UpdateContext(
        UpdateSource.githubUpdateSource("MrFast-js", "Skyblock-Tweaks"),
        UpdateTarget.deleteAndSaveInTheSameFolder(VersionManager::class.java),
        CurrentVersion.ofTag(MOD_VERSION),
        MOD_ID
    )

    class NeededUpdate {
        var versionName = ""
    }
    // /sbt update check
    //             pre|beta
    //             full|latest


    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        silentUpdateCheck()
    }

    // Silently checks if you are behind in version (once per launch)
    private fun silentUpdateCheck() {
        if (didSilentCheck || Utils.mc.thePlayer==null) return
        didSilentCheck = true

        checkPotentialUpdate {
            if (isClientOutdated()) {
                println("Found outdated SBT client sending update!")

                val updatePreference = getUpdatePreference()
                val updateVersion = "v" + potentialUpdate!!.update.versionName.split("v")[1].trim()

                neededUpdate.versionName = updateVersion

                if (CustomizationConfig.updateNotify) {
                    val notificationText =
                        ChatComponentText("§aVersion §6§l$updateVersion§r §ais available. §eClick to update!")
                            .setChatStyle(
                                ChatStyle()
                                    .setChatClickEvent(
                                        ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/sbt update $updatePreference"
                                        )
                                    )
                                    .setChatHoverEvent(
                                        HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            ChatComponentText("§aClick to update")
                                        )
                                    )
                            )

                    Utils.setTimeout({
                        Utils.playSound("random.orb", 0.1)
                        ChatUtils.logMessage(notificationText)
                    }, 10000)
                }
            }
        }
    }

    private fun checkPotentialUpdate(run: Runnable) {
        println("Checking for Skyblock Tweaks updates: " + getUpdatePreference())

        context.checkUpdate(getUpdatePreference()).thenAcceptAsync { update: PotentialUpdate? ->
            if (update != null) {
                println("Found potential SBT update! Version: ${update.update.versionName}")
                potentialUpdate = update
                run.run()
            }
        }
    }

    private fun isClientOutdated(): Boolean {
        val currentVersionValue: Double = getVersionValue(MOD_VERSION)
        val updateVersionName = potentialUpdate!!.update.versionName.split("v")[1]
        val updateVersionValue: Double = getVersionValue(updateVersionName)

        return currentVersionValue < updateVersionValue
    }

    private fun getVersionValue(version: String): Double {
        val mainVersionPart = version.split("-")[0]
        val mainVersionNum = mainVersionPart.replace("[^0-9]".toRegex(), "").toDouble()

        if ("BETA" in version) {
            val betaPart = version.split("-")[1]
            val betaNum = betaPart.replace("[^0-9]".toRegex(), "").toDouble()
            // 1.1.5-BETA5 (pre) -> 114.005
            return (mainVersionNum - 1) + (betaNum / 1000)
        } else {
            // 1.1.5 (full) -> 115
            return mainVersionNum
        }
    }

    private fun getUpdatePreference(): String {
        return CustomizationConfig.updateCheckType.split(" ")[0].lowercase()
    }

    // /sbt update check
    fun checkIfNeedUpdate() {
        checkPotentialUpdate {
            val updateVersion =
                potentialUpdate?.update?.versionName?.split("v")?.getOrNull(1)?.trim() ?: return@checkPotentialUpdate

            neededUpdate.versionName = updateVersion

            val currentVersionValue = getVersionValue(MOD_VERSION)
            val updateInfo = potentialUpdate!!.update
            val updateVersionName = updateInfo.versionName.split("v")[1]
            val updateVersionValue = getVersionValue(updateVersionName)

            ChatUtils.logMessage("§aCurrent version: §b${MOD_VERSION} $currentVersionValue")
            ChatUtils.logMessage("§aLatest version: §b$updateVersionName $updateVersionValue")
            when {
                currentVersionValue > updateVersionValue -> {
                    ChatUtils.logMessage("§aYou are using a more recent version. No update needed.")

                }

                currentVersionValue == updateVersionValue -> {
                    ChatUtils.logMessage("§aYou are already using the latest version. Enjoy!")
                }

                else -> {
                    val comp =
                        ChatComponentText("§eYou are using an outdated version. §aVersion §6§l$updateVersionName§r §ais available. §eClick to update!")
                            .setChatStyle(
                                ChatStyle().setChatClickEvent(
                                    ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sbt update latest")
                                )
                            )
                    ChatUtils.logMessage(comp)
                }
            }
        }
    }

    fun doUpdate() {
        CompletableFuture.supplyAsync<Any?> {
            try {
                ChatUtils.logMessage("§ePreparing update...")
                potentialUpdate!!.prepareUpdate()
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
            null
        }.thenAcceptAsync {
            try {
                ChatUtils.logMessage("§aDownloading update: §b" + potentialUpdate!!.update.versionName)
                potentialUpdate!!.executeUpdate()

                val notificationText =
                    ChatComponentText("§aUpdate downloaded successfully! SBT will update when you close the game. ")
                val closeGame = ChatComponentText("§c§l[CLOSE GAME]")
                    .setChatStyle(
                        ChatStyle()
                            .setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sbt update close"))
                            .setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§cClick to close the game")
                                )
                            )
                    )
                notificationText.appendSibling(closeGame)
                ChatUtils.logMessage(notificationText)
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }
    }
}