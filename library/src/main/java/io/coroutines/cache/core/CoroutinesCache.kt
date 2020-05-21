package io.coroutines.cache.core

import android.content.Context
import androidx.lifecycle.*
import com.google.gson.reflect.TypeToken
import io.coroutines.cache.dao.Cache
import io.coroutines.cache.dao.RealmDatabase
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.CoroutineContext

open class CoroutinesCache(
    private var context: Context,
    @PublishedApi internal val test: Boolean = false,
    @PublishedApi internal val lifecycleOwner: LifecycleOwner? = null,
    @PublishedApi internal val jsonMapper: JsonMapper = GsonMapper()
) : CoroutineScope {

    private val executionJob: Job by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + executionJob
    }

    @PublishedApi
    internal val database: RealmDatabase by lazy {
        RealmDatabase(context).apply {
            initDatabase()
        }
    }

    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun destroy() {
                database.deleteLifecycle()
            }
        })
    }

    fun clear() {
        database.clear()
    }

    fun deleteItem(key: String) {
        database.deleteItem(key)
    }


    inline fun <reified T : Any> asyncCache(
        noinline source: suspend () -> Deferred<T>,
        key: String,
        cachePolicy: CachePolicy
    ): Deferred<T> {
        return if (test) {
            async { source().await() }
        } else {
            when (cachePolicy) {
                is CachePolicy.TimeCache -> cacheTimeResolver(source, key, cachePolicy)
                is CachePolicy.EvictProvider -> cacheProviderResolver(source, key, cachePolicy)
                is CachePolicy.LifecycleCache -> cacheLifecycleResolver(source, key)
            }
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> cacheTimeResolver(
        noinline source: suspend () -> Deferred<T>,
        key: String,
        cachePolicy: CachePolicy.TimeCache
    ): Deferred<T> {
        getFromCacheValidateTime<T>(key, cachePolicy)?.let {
            return async { it }
        } ?: run {
            return getFromSource(source, key)
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> cacheProviderResolver(
        noinline source: suspend () -> Deferred<T>,
        key: String,
        cachePolicy: CachePolicy.EvictProvider
    ): Deferred<T> {
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

    @PublishedApi
    internal inline fun <reified T : Any> cacheLifecycleResolver(
        noinline source: suspend () -> Deferred<T>,
        key: String
    ): Deferred<T> {
        if (lifecycleOwner == null) {
            throw IllegalStateException("Necessary pass a lifecycleowner to Coroutines Cache Constructor")
        }

        return getFromCache<T>(key)?.let {
            async { it }
        } ?: run {
            getFromSource(source, key, true)
        }
    }

    @PublishedApi
    internal fun <T> getFromSource(
        source: suspend () -> Deferred<T>,
        key: String,
        isLifecycle: Boolean = false
    ): Deferred<T> {
        return async {
            val result = source().await()
            database.getDatabase().apply {
                beginTransaction()
                copyToRealmOrUpdate(
                    Cache(
                        key,
                        jsonMapper.toJson(result),
                        Calendar.getInstance().time,
                        isLifecycle
                    )
                )
                commitTransaction()
                close()
            }
            result
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> getFromCache(key: String): T? {
        val resultDb =
            database.getDatabase().where(Cache::class.java).equalTo("id", key).findFirst()?.data
        return if (resultDb != null) {
            val listType = object : TypeToken<T>() {}.type
            jsonMapper.fromJson(resultDb, listType)
        } else {
            return null
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> getFromCacheValidateTime(
        key: String,
        timeCache: CachePolicy.TimeCache
    ): T? {
        val resultDb =
            database.getDatabase().where(Cache::class.java).equalTo("id", key).findFirst()
        return if (resultDb != null) {
            if (resultDb.date.isValidCache(timeCache.duration, timeCache.timeUnit)) {
                val listType = object : TypeToken<T>() {}.type
                jsonMapper.fromJson(resultDb.data, listType) as T?
            } else {
                null
            }
        } else {
            null
        }
    }
}
