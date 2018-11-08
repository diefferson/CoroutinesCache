package io.coroutines.cache.dao

import android.arch.persistence.room.*

@Database(entities = [Cache::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

@Dao
interface CacheDao {

    @get:Query("SELECT * FROM cache")
    val all: List<Cache>

    @Query("SELECT * FROM cache WHERE id = :cacheId")
    fun get(cacheId: String): Cache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cache: Cache)

    @Delete
    fun delete(cache: Cache)
}

@Entity
class Cache (@PrimaryKey var id:String, var data:String)