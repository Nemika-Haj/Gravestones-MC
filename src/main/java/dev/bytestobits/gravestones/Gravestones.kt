package dev.bytestobits.gravestones

import dev.bytestobits.gravestones.commands.GraveCommand
import dev.bytestobits.gravestones.database.Core
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Gravestones : JavaPlugin() {
    val database = Core(this);

    fun colorMessage(text: String) = text.replace("&", "§")

    override fun onEnable() {
        Bukkit.getPluginCommand("grave")?.setExecutor(GraveCommand(this))
        Bukkit.getPluginManager().registerEvents(DeathEvent(this), this)
    }

    override fun onDisable() {
        database.connection.close()
    }
}