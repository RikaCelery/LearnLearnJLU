package service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import util.OsUtils
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object DB {
    object ConfTable : Table("configs") {
        val key = text("key").uniqueIndex()
        val value = text("value")
        override val primaryKey: PrimaryKey
            get() = PrimaryKey(key)
    }

    fun getValue(key:String):String?{
        return transaction{
            ConfTable.select {
                ConfTable.key eq key
            }.singleOrNull()?.get(ConfTable.value)
        }
    }
    fun setValue(key:String,value:String):String?{
        return transaction {
            val oldValue = ConfTable.select {
                ConfTable.key eq key
            }.singleOrNull()?.get(ConfTable.value)
            if (oldValue==null) {
                // 插入默认值
                ConfTable.insert {
                    it[ConfTable.key] = key
                    it[ConfTable.value] = value
                }
            } else {
                // 更新现有值
                ConfTable.update({ConfTable.key eq key}) {
                    it[ConfTable.value] = value
                }
            }
            oldValue
        }
    }

    // 设置数据库连接
    private fun setupDatabase() {
        val userHome =OsUtils.homeDir
        if (!Path("$userHome/.jlu-learn-tech/config.db").exists())
            Path("$userHome/.jlu-learn-tech").createDirectories()
        Database.connect("jdbc:sqlite:"+userHome + "/.jlu-learn-tech/config.db", "org.sqlite.JDBC")
    }

    // 创建表
    private fun createTable() {
        transaction {
            SchemaUtils.create(ConfTable)
        }
    }

    init {
        try{
            setupDatabase()
            createTable()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
actual fun save(key: String, value: String) :String?{
    return null
    return DB.setValue(key, value)
}

actual fun load(key: String): String? {
    return null
    return DB.getValue(key)
}