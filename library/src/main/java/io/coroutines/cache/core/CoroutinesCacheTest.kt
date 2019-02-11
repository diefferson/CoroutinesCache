package io.coroutines.cache.core

import android.content.Context
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class CoroutinesCacheTest(private var context: Context): CoroutinesCache,CoroutineScope{

    private val executionJob: Job  by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Unconfined + executionJob
    }

    inline fun <reified T:Any> asyncCache(noinline source: suspend ()->Deferred<T>, key: String, cachePolicy: CachePolicy): Deferred<T> {
        return async {source().await()}
    }
}
