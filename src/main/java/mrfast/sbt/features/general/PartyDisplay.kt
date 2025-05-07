package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.managers.PartyManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils

@SkyblockTweaks.EventComponent
object PartyDisplay {
    init {
        PartyDisplayGui()
    }

    class PartyDisplayGui : GuiManager.Element() {
        init {
            this.relativeX = 0.0
            this.relativeY = 0.39
            this.elementName = "Party Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("Party Members (5)")
            this.needsExample = true
        }

        override fun draw() {
            if (PartyManager.partyMembers.isEmpty()) return
            if (PartyManager.partyMembers.size == 1 && PartyManager.partyMembers.keys.contains(Utils.mc.thePlayer.name)) return

            var display = "§9§lParty Members §r§7(${PartyManager.partyMembers.size})"
            for (partyMember in PartyManager.partyMembers.values) {
                val leaderText = if (partyMember.leader) " §6♚" else ""
                var name = partyMember.name
                if (partyMember.name == Utils.mc.thePlayer.name) {
                    name = "§a${partyMember.name}"
                }
                display += "\n §e• §3${name}$leaderText"
                if (partyMember.className != "") {
                    display += " §e(${partyMember.className} ${partyMember.classLvl})"
                }
            }
            for ((index, s) in display.split("\n").withIndex()) {
                GuiUtils.drawText(s, 0f, (index * 10).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
            }
        }

        override fun drawExample() {
            val display = "§9§lParty Members §r§7(5)\n §e• §3MrFast §6♚\n §e• §3MrFast §e(Archer 20)\n §e• §3MrFast §e(Mage 20)\n §e• §3MrFast §e(Berserker 20)\n §e• §3MrFast §e(Tank 20)"
            for ((index, s) in display.split("\n").withIndex()) {
                GuiUtils.drawText(s, 0f, (index * 10).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.partyMemberDisplay
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}