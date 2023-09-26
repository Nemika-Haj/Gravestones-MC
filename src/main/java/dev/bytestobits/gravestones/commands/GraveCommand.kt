package dev.bytestobits.gravestones.commands

import dev.bytestobits.gravestones.Gravestones
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GraveCommand(private val plugin: Gravestones): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(sender !is Player) {
            sender.sendMessage("Only players can execute this command.")
            return true
        }

        plugin.database.connection.let { conn ->
            val sql = "SELECT * FROM gravestones WHERE uuid = ?"
            val statement = conn.prepareStatement(sql)
            statement.setString(1, sender.uniqueId.toString())

            val resultSet = statement.executeQuery()
            val graveLocations = mutableListOf(
                "&7&lGravestone &alocations:"
            )

            while(resultSet?.next() == true) {
                val x = resultSet.getInt("x")
                val y = resultSet.getInt("y")
                val z = resultSet.getInt("z")
                graveLocations.add("&e$x, $y, $z")
            }

            sender.sendMessage(graveLocations.joinToString("\n") { plugin.colorMessage(it) })
        }

        return true
    }

}