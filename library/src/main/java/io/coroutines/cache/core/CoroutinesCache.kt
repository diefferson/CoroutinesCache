package io.coroutines.cache.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.coroutines.cache.dao.Cache
import io.coroutines.cache.dao.RealmDatabase
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

open class CoroutinesCache(private var context: Context): CoroutineScope{

    private val executionJob: Job  by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + executionJob
    }

    val database:RealmDatabase by lazy {
        RealmDatabase(context).apply {
            initDatabase()
        }
    }
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
            database.getDatabase().apply {
                beginTransaction()
                copyToRealmOrUpdate(Cache(key,  Gson().toJson(result), Calendar.getInstance().time))
                commitTransaction()
                close()
            }
            result
        }
    }

    inline fun <reified T:Any> getFromCache(key: String): T? {
        val resultDb = database.getDatabase().where(Cache::class.java).equalTo("id", key).findFirst()?.data
        return if (resultDb != null) {
            val listType = object : TypeToken<T>() {}.type
            Gson().fromJson(resultDb, listType) as T
        } else {
            return null
        }
    }

    companion object {
        const val CACHE_PREFIX = "cache"
    }
}
