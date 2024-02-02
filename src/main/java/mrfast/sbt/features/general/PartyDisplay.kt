package mrfast.sbt.features.general

import mrfast.sbt.apis.PartyManager
import mrfast.sbt.config.Categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils

object PartyDisplay {
    init {
        PartyDisplayGui()
    }

    class PartyDisplayGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Party Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            if(PartyManager.partyMembers.isEmpty()) return
            if(PartyManager.partyMembers.size==1 && PartyManager.partyMembers.keys.contains(Utils.mc.thePlayer.name)) return

            var display = "§9§lParty Members §r§7(${PartyManager.partyMembers.size})"
            for (partyMember in PartyManager.partyMembers.values) {
                var name = if (partyMember.leader) "§n"+partyMember.name else partyMember.name
                if(partyMember.name.equals(Utils.mc.thePlayer.name)) {
                    name = "§a${partyMember.name}"
                }
                display += "\n §e• §3${name}"
                if (partyMember.className!="") {
                    display += " §e(${partyMember.className} ${partyMember.classLvl})"
                }
            }
            for ((index, s) in display.split("\n").withIndex()) {
                GuiUtils.drawText(s, 0f, (index * 10).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
            }
            this.width = Utils.mc.fontRendererObj.getStringWidth(display) + 1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.partyMemberDisplay
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}