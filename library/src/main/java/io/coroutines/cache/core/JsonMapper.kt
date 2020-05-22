package io.coroutines.cache.core

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.StringReader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface JsonMapper {
    fun toJson(src: Any): String
    fun <T> fromJson(json: String, typeOfT: Type): T
    fun <T> assertValidJson(result: T): Boolean
}

class GsonRetrofitMapper : JsonMapper {
    override fun toJson(src: Any): String {
        val response = src as Response<*>
        val body = response.body()!!
        return Gson().toJson(body, body.javaClass)
    }

    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(json: String, retrofitResponse: Type): T {
        val reader = StringReader(json)
        val parameterizedType = retrofitResponse as ParameterizedType
        val storedType = parameterizedType.actualTypeArguments[0]
        val storedJson = Gson().fromJson(reader, storedType) as Any
        return Response.success(storedJson) as T
    }

    override fun <T> assertValidJson(result: T): Boolean {
        if (result is Response<*>) {
            if (result.isSuccessful) {
                return true
            }
        }
        return false
    }
}
