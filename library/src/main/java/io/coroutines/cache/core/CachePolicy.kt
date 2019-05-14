package io.coroutines.cache.core

import java.util.concurrent.TimeUnit

sealed class CachePolicy{
    data class EvictProvider(val fromSource:Boolean) :CachePolicy()
    data class TimeCache(val duration: Int, val timeUnit: TimeUnit):CachePolicy()
    object LifecycleCache:CachePolicy()
}