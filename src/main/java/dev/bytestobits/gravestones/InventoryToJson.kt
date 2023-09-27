package dev.bytestobits.gravestones

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object InventoryToJson {
    private val gson = Gson()

    fun convert(items: Array<ItemStack?>): String {
        val jsonItems = JsonArray()

        items.forEach { item ->
            item?.let {
                val jsonItem = JsonObject()
                jsonItem.addProperty("type", it.type.name)
                jsonItem.addProperty("amount", it.amount)

                val enchantments = JsonObject()
                it.enchantments.forEach { (enchantment, level) ->
                    enchantments.addProperty(enchantment.key.toString(), level)
                }
                jsonItem.add("enchantments", enchantments)

                if(it.type.maxDurability > 0) {
                    val meta = it.itemMeta as Damageable
                    val durability = meta.damage
                    jsonItem.addProperty("durability", durability)
                }

                if(it.itemMeta != null && it.itemMeta.hasCustomModelData()) {
                    jsonItem.addProperty("customData", it.itemMeta.customModelData)
                }

                jsonItems.add(jsonItem)
            }
        }

        return gson.toJson(jsonItems)
    }

    fun giveToPlayer(items: String, player: Player) {
        val jsonArray: JsonArray = gson.fromJson(items, JsonArray::class.java)

        jsonArray.forEach { item ->
            val jsonObject = item as JsonObject

            val type = Material.valueOf(jsonObject.get("type").asString)
            val amount = jsonObject.get("amount").asInt
            val itemStack = ItemStack(type, amount)

            val enchantments = jsonObject.getAsJsonObject("enchantments")
            enchantments.entrySet().forEach { (enchantmentKey, level) ->
                val enchantment = Enchantment.getByKey(NamespacedKey.fromString(enchantmentKey.lowercase()))
                enchantment?.let {
                    itemStack.addUnsafeEnchantment(it, level.asInt)
                }
            }

            if (itemStack.type.maxDurability > 0) {
                val meta = itemStack.itemMeta as Damageable
                meta.damage = jsonObject.get("durability").asInt
            }

            if(jsonObject.has("customData")) {
                val meta = itemStack.itemMeta
                meta.setCustomModelData(jsonObject.get("customData").asInt)
                itemStack.itemMeta = meta
            }

            if(isInventoryFull(player)) {
                player.location.world.dropItemNaturally(player.location, itemStack)
            } else {
                player.inventory.addItem(itemStack)
            }
        }
    }

    private fun isInventoryFull(player: Player): Boolean {
        val inventory = player.inventory
        return inventory.storageContents.all { it != null }
    }
}