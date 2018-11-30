package io.coroutines.cache.core

import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.isValidCache( duration: Int, timeUnit: TimeUnit): Boolean {
    val currentDate = Calendar.getInstance()
    val cacheDate = Calendar.getInstance().apply {
        time = this@isValidCache
    }

    cacheDate.add(getCalendarTimeUnit(timeUnit), duration)
    return currentDate.before(cacheDate)
}

fun  getCalendarTimeUnit(timeUnit: TimeUnit):Int{
    return when(timeUnit){
        TimeUnit.NANOSECONDS -> throw Exception("NANOSECONDS is not supported from cache")
        TimeUnit.MICROSECONDS ->  throw Exception("MICROSECONDS is not supported from cache")
        TimeUnit.MILLISECONDS -> Calendar.MILLISECOND
        TimeUnit.SECONDS -> Calendar.SECOND
        TimeUnit.MINUTES -> Calendar.MINUTE
        TimeUnit.HOURS -> Calendar.HOUR
        TimeUnit.DAYS -> Calendar.DAY_OF_YEAR
    }
}