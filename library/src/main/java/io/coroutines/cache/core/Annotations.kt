package io.coroutines.cache.core

import java.util.concurrent.TimeUnit


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LifeCache(val duration: Long, val timeUnit: TimeUnit)
