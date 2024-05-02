package net.blockventuremc

import de.themeparkcraft.audioserver.common.data.RabbitConfiguration
import de.themeparkcraft.audioserver.minecraft.AudioServer
import dev.kord.core.Kord
import io.github.cdimascio.dotenv.dotenv
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.database.DatabaseManager
import net.blockventuremc.modules.discord.DiscordBot
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.placeholders.PlayerPlaceholderManager
import net.blockventuremc.utils.RegisterManager.registerMC
import net.blockventuremc.utils.mcasyncBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class VentureLibs : JavaPlugin() {
    companion object {
        lateinit var instance: VentureLibs
        lateinit var bot: DiscordBot
    }

    val dotenv = dotenv()

    init {
        instance = this
    }

    override fun onLoad() {
        server.spigot().spigotConfig["messages.unknown-command"] = "§c" + "Unknown Command"
        server.spigot().spigotConfig["messages.server-full"] = "server full - Club Members can join at any time"
        server.spigot().spigotConfig["messages.outdated-client"] =
            "Your client is outdated, please use the latest version of Minecraft"
        server.spigot().spigotConfig["messages.outdated-server"] =
            "Hold on! We are not that fast. We upgrade as soon as we can"
    }

    override fun onEnable() {
        logger.info("Loading database...")
        DatabaseManager.database

        DatabaseManager.register()

        logger.info("Loading translations...")
        TranslationCache.loadAll()


        logger.info("Connecting to audioserver...")
        AudioServer.connect(
            RabbitConfiguration(
                dotenv["RABBITMQ_HOST"] ?: "localhost",
                dotenv["RABBITMQ_PORT"]?.toInt() ?: 5672,
                dotenv["RABBITMQ_VHOST"] ?: "/",
                dotenv["RABBITMQ_USER"] ?: "guest",
                dotenv["RABBITMQ_PASSWORD"] ?: "guest"
            )
        )


        logger.info("Registering placeholders...")
        PlayerPlaceholderManager()


        logger.info("Registering modules...")
        registerMC()

        PlayerCache.runOnlineTimeScheduler()

        logger.info("Starting Discord bot...")

        if (dotenv["BOT_TOKEN"] != null) mcasyncBlocking {
            val kord = Kord(dotenv["BOT_TOKEN"]!!)

            bot = DiscordBot(kord)
            bot.start()
        } else {
            logger.warning("BOT_TOKEN is not set in .env file, Discord bot will not be started")
        }

        logger.info("Hello, Minecraft!")
    }

    override fun onDisable() {
        PlayerCache.cleanup()

        for (player in Bukkit.getOnlinePlayers()) {
            val pixelPlayer = PlayerCache.getOrNull(player.uniqueId) ?: continue
            PlayerCache.saveToDB(pixelPlayer.copy(username = player.name))
        }

        AudioServer.disconnect()

        logger.info("Plugin has been disabled")
    }
}