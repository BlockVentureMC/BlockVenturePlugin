package net.blockventuremc.utils

import net.blockventuremc.Plugin
import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.consts.NAMESPACE_PLUGIN
import net.blockventuremc.extensions.sendMessagePrefixed
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.reflections8.Reflections
import kotlin.time.measureTime

object RegisterManager {
    fun registerAll() {
        val reflections = Reflections("net.blockventuremc.modules")

        val timeListeners = measureTime {
            for (clazz in reflections.getSubTypesOf(Listener::class.java)) {
                try {
                    val constructor = clazz.declaredConstructors.find { it.parameterCount == 0 } ?: continue

                    if (clazz.`package`.name.contains("conversations")) continue

                    constructor.isAccessible = true

                    val event = constructor.newInstance() as Listener

                    Bukkit.getPluginManager().registerEvents(event, Plugin.instance)
                    Bukkit.getConsoleSender()
                        .sendMessage("Listener ${event.javaClass.simpleName} registered")
                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                }
            }
        }
        println("Registered listeners in $timeListeners")


        val timeCommands = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(BlockCommand::class.java)) {
                try {
                    val annotation: BlockCommand = clazz.getAnnotation(BlockCommand::class.java)

                    val pluginClass: Class<PluginCommand> = PluginCommand::class.java
                    val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

                    constructor.isAccessible = true

                    val command: PluginCommand = constructor.newInstance(annotation.name, Plugin.instance)

                    command.aliases = annotation.aliases.toList()
                    command.description = annotation.description
                    command.permission = Permission(annotation.permission, annotation.permissionDefault).name
                    command.usage = annotation.usage
                    val commandInstance = clazz.getDeclaredConstructor().newInstance() as CommandExecutor
                    command.setExecutor { sender, command, label, args ->
                        try {
                            commandInstance.onCommand(sender, command, label, args)
                        } catch (e: Exception) {
                            sender.sendMessagePrefixed("Ein Fehler ist aufgetreten!")
                            throw e
                        }
                    }
                    command.tabCompleter = commandInstance as? org.bukkit.command.TabCompleter


                    Bukkit.getCommandMap().register(NAMESPACE_PLUGIN, command)
                    Bukkit.getConsoleSender().sendMessage("Command ${command.name} registered")
                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                }
            }
        }
        println("Registered commands in $timeCommands")
    }
}