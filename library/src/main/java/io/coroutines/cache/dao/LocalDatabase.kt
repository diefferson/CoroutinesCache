package io.coroutines.cache.dao

import android.content.Context
import io.coroutines.cache.core.CoroutinesCache
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class RealmDatabase(var context: Context) {

    fun initDatabase() {
        Realm.init(context)

        val realmConfiguration = RealmConfiguration.Builder()
            .name(context.packageName+ CoroutinesCache.CACHE_PREFIX)
            .schemaVersion(1)
            .build()

        Realm.setDefaultConfiguration(realmConfiguration)

    }

    fun getDatabase(): Realm {
        return Realm.getDefaultInstance()
    }

}

open class Cache constructor(): RealmObject(){

    @PrimaryKey
    var id:String = ""
    var data:String = ""

    constructor(id:String, data:String) :this(){
        this.id = id
        this.data = data
    }
}