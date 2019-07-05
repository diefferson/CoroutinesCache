package io.coroutines.cache.dao

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

class RealmDatabase(var context: Context) {

    fun initDatabase() {
        Realm.init(context)

        val realmConfiguration = RealmConfiguration.Builder()
            .name(context.packageName+CACHE_PREFIX)
            .schemaVersion(VERSION)
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(realmConfiguration)
    }

    fun getDatabase(): Realm {
        return Realm.getDefaultInstance()
    }

    fun deleteItem(key:String){
        getDatabase().apply {
            beginTransaction()
            val result = where(Cache::class.java).equalTo("id", key).findAll()
            result.deleteAllFromRealm()
            commitTransaction()
            close()
        }
    }

    fun deleteLifecycle(){
        getDatabase().apply {
            beginTransaction()
            val result = where(Cache::class.java).equalTo("lifecycle", true).findAll()
            result.deleteAllFromRealm()
            commitTransaction()
            close()
        }
    }

    fun clear(){
        getDatabase().apply {
            beginTransaction()
            deleteAll()
            commitTransaction()
            close()
        }
    }

    companion object {
        private const val VERSION = 1L
        private const val CACHE_PREFIX = "cache"
    }
}

open class Cache constructor(): RealmObject(){

    @PrimaryKey
    var id:String = ""
    var data:String = ""
    var date:Date = Calendar.getInstance().time
    var lifecycle:Boolean = false

    constructor(id:String, data:String, date: Date, lifecycle:Boolean = false) :this(){
        this.id = id
        this.data = data
        this.date = date
        this.lifecycle = lifecycle
    }
}