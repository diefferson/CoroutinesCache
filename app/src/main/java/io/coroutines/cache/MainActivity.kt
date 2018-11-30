package io.coroutines.cache

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.coroutines.cache.core.CachePolicy
import io.coroutines.cache.core.CoroutinesCache
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val executionJob: Job by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Main + executionJob
    }

    private val coroutinesCache :CoroutinesCache by lazy {
        CoroutinesCache(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launch {
            delay(3000)
            val myAsyncTet = coroutinesCache.asyncCache({ async { "My Async text!" } }, "Key", CachePolicy.LifeCache(10, TimeUnit.SECONDS)).await()

            withContext(Dispatchers.Main){
                text.text = myAsyncTet
            }
        }
    }
}
