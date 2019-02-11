package io.coroutines.cache.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.coroutines.cache.dao.Cache
import io.coroutines.cache.dao.RealmDatabase
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

open class CoroutinesCache(private var context: Context, val test: Boolean = false):CoroutineScope{

    private val executionJob: Job  by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + executionJob
    }

    @PublishedApi internal val database:RealmDatabase by lazy {
        RealmDatabase(context).apply {
            initDatabase()
        }
    }

    inline fun <reified T:Any> asyncCache(noinline source: suspend ()->Deferred<T>, key: String, cachePolicy: CachePolicy): Deferred<T> {
        return if(test){
            async{source().await()}
        }else{
            when(cachePolicy){

                is CachePolicy.LifeCache-> cacheLifeResolver( source, key, cachePolicy)
                is CachePolicy.EvictProvider -> cacheProviderResolver(source, key, cachePolicy)
            }
        }
    }

    @PublishedApi internal inline fun <reified T : Any> cacheLifeResolver(noinline source: suspend () -> Deferred<T>, key: String, cachePolicy: CachePolicy.LifeCache): Deferred<T> {
        getFromCacheValidateTime<T>(key, cachePolicy)?.let {
            return async { it }
        } ?: run {
            return getFromSource(source, key)
        }
    }

    @PublishedApi internal inline fun <reified T : Any> cacheProviderResolver(noinline source: suspend () -> Deferred<T>, key: String, cachePolicy: CachePolicy.EvictProvider): Deferred<T> {
        return if (cachePolicy.fromSource) {
            getFromSource(source, key)
        } else {
            getFromCache<T>(key)?.let {
                return async { it }
            } ?: run {
                return getFromSource(source, key)
            }
        }
    }

    @PublishedApi internal fun <T> getFromSource(source: suspend ()->Deferred<T>, key: String):Deferred<T>{
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

    @PublishedApi internal inline fun <reified T:Any> getFromCache(key: String): T? {
        val resultDb = database.getDatabase().where(Cache::class.java).equalTo("id", key).findFirst()?.data
        return if (resultDb != null) {
            val listType = object : TypeToken<T>() {}.type
            Gson().fromJson(resultDb, listType) as T
        } else {
            return null
        }
    }

    @PublishedApi internal inline fun <reified T:Any> getFromCacheValidateTime(key: String, lifeCache: CachePolicy.LifeCache): T? {
        val resultDb = database.getDatabase().where(Cache::class.java).equalTo("id", key).findFirst()
        return if (resultDb != null) {
            if(resultDb.date.isValidCache(lifeCache.duration, lifeCache.timeUnit)){
                val listType = object : TypeToken<T>() {}.type
                Gson().fromJson(resultDb.data, listType) as T
            }else{
                null
            }
        } else {
            null
        }
    }
}
