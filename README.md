[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CoroutinesCache-blue.svg?style=flat)](https://android-arsenal.com/details/1/7297)

# CoroutinesCache

Kotlin Coroutines is simple and your Cache Handler must be too, thats why I created this library, a powerfull caching library for Kotlin Android 

Every Android application is a client application, which means it does not make sense to create and maintain a database just for caching data.

Plus, the fact that you have some sort of legendary database for persisting your data does not solves by itself the real challenge: to be able to configure your caching needs in a flexible and simple way. 

Inspired by [Retrofit](http://square.github.io/retrofit/) api and [RxCache](https://github.com/diefferson/RxCache), **CoroutinesCache is a reactive caching library for Android which turns your caching needs into simple functions.** 

When supplying an **`deferred` (these is the actualy supported Couroutine type)** which contains the data provided by an expensive task -probably an http connection, CoroutinesCache determines if it is needed to execute request to it or instead fetch the data previously cached. This decision is made based on the CachePolicy.
 
```kotlin
  myCache.asyncCache(source = suspend{restApi.getUser()},key =  "userKey", CachePolicy.LifeCache(15, TimeUnit.MINUTES))
```

## Setup

Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:
```gradle
dependencies {
    implementation "com.github.diefferson:CoroutinesCache:0.2.0"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:0.26.1-eap13"
}
```
## Usage

Using [Kotlin Coroutine Adapter](https://github.com/JakeWharton/retrofit2-kotlin-coroutines-adapter) create your retrofit interface:

```kotlin
interface RestApi {

    @GET("user/{id}")
    fun getUser(@Path("id")idUser:String): Deferred<User>

    @GET("user")
    fun getUsers(): Deferred<List<User>>
}

```
CoroutinesCache exposes `asyncCache()` method to help use cache in a row. 

### Build an instance of CouroutinesCache and call  `asyncCache()` method

Finally, instantiate the CouroutinesCache .

```kotlin
  val myCache = CoroutinesCache(context)
  
  //CachePolicy.EvictProvider to defines to local cache ou data source 
  val users:List<User> = myCache.asyncCache({restApi.getUsers()} , "usersKey", CachePolicy.EvictProvider(true)).await()
    
   //CachePolicy.LifeCache to defines a time to expire cache
  val user:User = myCache.asyncCache({restApi.getUser(id)} , "userKey"+id, CachePolicy.LifeCache(15, TimeUnit.MINUTES)).await()
  
```

## Author

**Diefferson Santos**

* <https://www.linkedin.com/in/diefferson-inocencio-santos-582066ba/>
* <https://github.com/diefferson>
