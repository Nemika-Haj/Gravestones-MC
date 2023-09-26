package dev.bytestobits.gravestones

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.roundToInt

class DeathEvent(private val plugin: Gravestones): Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val entity = event.entity
        val cause = entity.lastDamageCause?.cause

        if(cause == EntityDamageEvent.DamageCause.VOID) return
        if (entity.inventory.isEmpty) return

        val block = findNearestAirBlock(entity.location)
        if(block == null) {
            entity.sendMessage(plugin.colorMessage("&cCould not find any empty space to save your gravestone, so your items dropped normally."))
            return
        }

        event.drops.clear()
        event.droppedExp = 0

        plugin.database.createGravestone(entity.uniqueId.toString(), block.location, entity.inventory.contents)
        plugin.logger.info("Saved inventory of ${entity.name} to Gravestone at ${entity.location}")

        block.type = Material.STONE_BRICK_WALL
        Holograms.putHologram(Location(block.world, block.x.toDouble(), block.y.toDouble(), block.z.toDouble()), "&f&l${entity.name}'s Gravestone")

        entity.sendMessage(plugin.colorMessage("&eA &7Gravestone &ewas created at ${block.location.x.toInt()}, ${block.location.y.toInt()}, ${block.location.z.toInt()}"))
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if(event.action == Action.LEFT_CLICK_BLOCK) {
            val block = event.clickedBlock
            if(block === null) return
            val blockLocation = block.location

            plugin.database.connection.let { conn ->
                val sql = "SELECT * FROM gravestones WHERE x = ? AND y = ? AND z = ?"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, blockLocation.x.roundToInt())
                statement.setInt(2, blockLocation.y.roundToInt())
                statement.setInt(3, blockLocation.z.roundToInt())

                val resultSet = statement.executeQuery()

                while(resultSet?.next() == true) {
                    val uuid = resultSet.getString("uuid")
                    event.isCancelled = true

                    if(uuid != event.player.uniqueId.toString()) {
                        event.player.sendMessage(plugin.colorMessage("&cYou cannot open this gravestone since it does not belong to you."))
                    } else {
                        block.type = Material.AIR
                        InventoryToJson.giveToPlayer(resultSet.getString("items"), event.player)
                        plugin.database.removeGravestone(resultSet.getInt("id"))
                        Holograms.removeHologram(Location(block.world, block.x.toDouble(), block.y.toDouble(), block.z.toDouble()))
                        event.player.sendMessage(plugin.colorMessage("&aYou have opened your gravestone!"))
                    }
                }
            }
        }
    }

    private fun findNearestAirBlock(startLocation: Location): Block? {
        val world = startLocation.world
        val radius = 10

        for (x in 0..radius) {
            for (y in 0..radius) {
                for (z in 0..radius) {
                    val blockLocation = startLocation.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                    val block = world.getBlockAt(blockLocation)

                    if (block.type == Material.AIR) {
                        return block.location.block
                    }
                }
            }
        }

        for (x in -radius..-1) {
            for (y in -radius..-1) {
                for (z in -radius..-1) {
                    val blockLocation = startLocation.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                    val block = world.getBlockAt(blockLocation)

                    if (block.type == Material.AIR) {
                        return block.location.block
                    }
                }
            }
        }

        return null
    }

    // Big secret
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        event.drops.removeIf { it.type == Material.ROTTEN_FLESH }
    }
}