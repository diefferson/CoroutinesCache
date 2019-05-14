package io.coroutines.cache

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.coroutines.cache.core.CachePolicy
import io.coroutines.cache.core.CoroutinesCache
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
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
            val myAsyncTet = coroutinesCache.asyncCache({ async { "My Async text!" } }, "Key",CachePolicy.LifecycleCache).await()

            withContext(Dispatchers.Main){
                text.text = myAsyncTet
            }
        }

        text.setOnClickListener {
            startActivity(Intent(this, Main2Activity::class.java))
        }
    }
}
