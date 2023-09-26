package dev.bytestobits.gravestones

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType

object Holograms {

    private fun modifyLocation(location: Location) {
        location.add(0.5, 1.5, 0.5)
    }

    fun putHologram(location: Location, text: String) {
        modifyLocation(location)
        val armorStand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isVisible = false
        armorStand.isCustomNameVisible = true
        armorStand.customName(Component.text(text.replace("&", "§")))
        armorStand.isInvulnerable = true
        armorStand.isCollidable = false
        armorStand.isMarker = true
        armorStand.setGravity(false)
        armorStand.teleport(location)
    }

    fun removeHologram(location: Location) {
        modifyLocation(location)
        val hologram = location.world.getNearbyEntities(location, 5.0, 5.0, 5.0).firstOrNull { it is ArmorStand }

        hologram?.remove()
    }
}