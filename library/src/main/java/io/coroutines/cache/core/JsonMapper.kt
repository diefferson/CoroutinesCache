package io.coroutines.cache.core

import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonSyntaxException
import java.io.StringReader
import java.lang.reflect.Type

interface JsonMapper {
    fun toJson(src: Any?): String
    fun <T> fromJson(json: String?, typeOfT: Type?): T?
}

class GsonMapper : JsonMapper {
    override fun toJson(src: Any?): String {
        return if (src == null) {
            Gson().toJson(JsonNull.INSTANCE)
        } else Gson().toJson(src, src.javaClass)
    }

    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(json: String?, typeOfT: Type?): T? {
        if (json == null) {
            return null
        }
        val reader = StringReader(json)
        return Gson().fromJson(reader, typeOfT!!) as T
    }

}
