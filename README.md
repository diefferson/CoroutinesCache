
# CoroutinesCache

The **goal** of this library is simple: **caching your data models like [Picasso](https://github.com/square/picasso) caches your images, with no effort at all.** 

Every Android application is a client application, which means it does not make sense to create and maintain a database just for caching data.

Plus, the fact that you have some sort of legendary database for persisting your data does not solves by itself the real challenge: to be able to configure your caching needs in a flexible and simple way. 

Inspired by [Retrofit](http://square.github.io/retrofit/) api and [RxCache](https://github.com/diefferson/RxCache), **CoroutinesCache is a reactive caching library for Android which turns your caching needs into simple functions.** 

When supplying an **`deferred` (these is the actualy supported Couroutine type)** which contains the data provided by an expensive task -probably an http connection, CoroutinesCache determines if it is needed 
to execute request to it or instead fetch the data previously cached. This decision is made based on the functions parameters.
 
```kotlin
  myCache.asyncCache(source = suspend{restApi.getUser()},key =  "terms", forceSource = false)
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
    implementation "com.github.diefferson:CoroutinesCache:0.0.5"
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
  
  val users:List<User> = myCache.asyncCache(suspend{restApi.getUsers()} , "usersKey", false).await()
  
  val user:User = myCache.asyncCache(suspend{restApi.getUser(id)} , "userKey"+id, false).await()
  
```

## Author

**Diefferson Santos**

* <https://www.linkedin.com/in/diefferson-inocencio-santos-582066ba/>
* <https://github.com/diefferson>
