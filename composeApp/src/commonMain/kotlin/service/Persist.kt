package service

object Persist {
    fun dbsave(key: String, value: String){
        save(key, value)
    }
    fun dbload(key: String): String?{
        return load(key)
    }
}

expect fun save(key: String, value: String):String?
expect fun load(key: String):String?