package net.blockventuremc.modules.general.events.custom

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class AreaLeaveEvent(area: Area, val player: Player) : AreaEvent(area) {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}