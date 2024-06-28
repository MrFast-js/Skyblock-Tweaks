package mrfast.sbt.customevents

import mrfast.sbt.apis.ItemAbilities
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class UseItemAbilityEvent(ability: ItemAbilities.ItemAbility) : Event() {
    var ability: ItemAbilities.ItemAbility

    init {
        this.ability = ability
    }

    override fun isCancelable(): Boolean {
        return true
    }
}