package net.blockventuremc.modules.placeholders

import dev.fruxz.stacked.text
import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.api.placeholder.Placeholder
import me.neznamy.tab.api.placeholder.PlayerPlaceholder
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.entity.Player


/**
 * Manages player placeholders.
 */
class PlayerPlaceholderManager {

    /**
     * Represents a list of player placeholders.
     */
    private val placeholders = mutableListOf<Placeholder>()

    init {
        reloadPlaceholders()
        setupReload()
    }

    /**
     * Sets up the reload functionality for the player placeholders.
     * This method registers TabLoadEvent listener and reloads the placeholders.
     * @see PlayerPlaceholderManager.reloadPlaceholders
     */
    fun setupReload() {
        TabAPI.getInstance().getEventBus()?.register(TabLoadEvent::class.java) { _ ->
            reloadPlaceholders()
        }
    }

    /**
     * Registers the placeholders for the player.
     */
    fun registerPlaceholders() {
        placeholders.clear()

        val placeholderManager = TabAPI.getInstance().placeholderManager

        placeholders += placeholderManager.registerPlayerPlaceholder("%rank%", 5000) { player ->
            (player.player as Player).toDatabaseUser().rank
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%rankord%", 5000) { player ->
            (player.player as Player).toDatabaseUser().rank.ordinal
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%color%", 5000) { player ->
            (player.player as Player).toDatabaseUser().rank.color
        }

        placeholders += placeholderManager.registerRelationalPlaceholder("%rel_title%", 5000) { player, viewer ->
            val title = (player.player as Player).toDatabaseUser().selectedTitle
            return@registerRelationalPlaceholder if (title == null) {
                (viewer.player as Player).translate("title.none")?.message ?: "<color:#4b6584>No title"
            } else {
                title.display(viewer.player as Player)
            }
        }
    }

    /**
     * Reloads the placeholders for the player.
     */
    fun reloadPlaceholders() {
        registerPlaceholders()
        Bukkit.getOnlinePlayers().forEach { player ->
            player.kick(text("TAB reload. Please rejoin."))
        }
        getLogger().info("TAB placeholders have been re-registered!")
    }
}

