package dev.bytestobits.gravestones

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object InventoryToJson {
    fun convert(items: Array<ItemStack?>): String {
        val itemStacksConfig = YamlConfiguration()

        items.forEachIndexed { index, itemStack ->
            itemStack?.let {
                itemStacksConfig.set("$index", itemStack)
            }
        }

        return itemStacksConfig.saveToString()
    }

    fun giveToPlayer(itemConfig: String, player: Player) {
        val itemStacksConfig = YamlConfiguration()
        itemStacksConfig.loadFromString(itemConfig)

        itemStacksConfig.getKeys(false).forEach { itemKey ->
            val item = itemStacksConfig.getItemStack(itemKey)

            item?.let {
                if(isInventoryFull(player)) {
                    player.location.world.dropItemNaturally(player.location, it)
                } else {
                    player.inventory.addItem(it)
                }
            }
        }
    }

    private fun isInventoryFull(player: Player): Boolean {
        val inventory = player.inventory
        return inventory.storageContents.all { it != null }
    }
}