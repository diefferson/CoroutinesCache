package io.coroutines.cache.core

import android.arch.persistence.room.Room
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.coroutines.cache.dao.Cache
import io.coroutines.cache.dao.LocalDatabase
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

open class CoroutinesCache(private var context: Context): CoroutineScope{

    private val executionJob: Job  by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + executionJob
    }

    fun getDataBase() =  Room.databaseBuilder(context, LocalDatabase::class.java, context.packageName+CACHE_PREFIX).build().cacheDao()

    inline fun <reified T:Any> asyncCache(noinline source: suspend ()->Deferred<T>, key: String, forceSource:Boolean): Deferred<T> {
        return if (forceSource) {
            getFromSource(source, key)
        } else {
            getFromCache<T>(key)?.let {
                return async { it }
            }?: run {
                return getFromSource(source, key)
            }
        }
    }

    fun <T> getFromSource(source: suspend ()->Deferred<T>, key: String):Deferred<T>{
        return async {
            val result = source().await()
            getDataBase().insert(Cache(key, Gson().toJson(result)))
            result
        }
    }

    inline fun <reified T:Any> getFromCache(key: String): T? {
        val resultDb = getDataBase().get(key)?.data
        return if (resultDb != null) {
            val listType = object : TypeToken<T>() {}.type
            Gson().fromJson(resultDb, listType) as T
        } else {
            return null
        }
    }

    companion object {
        private const val CACHE_PREFIX = "cache"
    }
}
