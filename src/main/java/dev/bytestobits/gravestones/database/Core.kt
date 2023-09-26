package dev.bytestobits.gravestones.database

import dev.bytestobits.gravestones.Gravestones
import dev.bytestobits.gravestones.InventoryToJson
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.sql.Connection
import java.sql.DriverManager
import kotlin.math.roundToInt

class Core(plugin: Gravestones) {
    val connection: Connection

    // UUID, X, Y, Z, ITEMSJSON
    init {
        val dbFile = plugin.dataFolder.resolve("gravestones.db")
        if(!dbFile.exists()) {
            plugin.dataFolder.mkdirs()
            dbFile.createNewFile()
        }

        val uri = "jdbc:sqlite:${dbFile.absolutePath}"

        connection = DriverManager.getConnection(uri)

        connection?.let { conn ->
            val statement = conn.createStatement()
            statement.execute("CREATE TABLE IF NOT EXISTS gravestones(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, items TEXT NOT NULL)")
        }
    }

    fun createGravestone(uuid: String, location: Location, inventory: Array<ItemStack?>) {
        val sql = "INSERT INTO gravestones(uuid, x, y, z, items) VALUES(?,?,?,?,?)"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, uuid)
        statement.setInt(2, location.x.roundToInt())
        statement.setInt(3, location.y.roundToInt())
        statement.setInt(4, location.z.roundToInt())
        statement.setString(5, InventoryToJson.convert(inventory))

        statement.executeUpdate()
    }

    fun removeGravestone(id: Int) {
        val sql = "DELETE FROM gravestones WHERE id = ?"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1, id)
        statement.executeUpdate()
    }

}