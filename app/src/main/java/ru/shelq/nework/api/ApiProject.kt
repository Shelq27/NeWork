package ru.shelq.nework.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.shelq.nework.dto.Post
import ru.shelq.nework.BuildConfig
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.User
import java.util.concurrent.TimeUnit

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
private const val API_KEY = BuildConfig.API_KEY

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .let {
        if (BuildConfig.DEBUG) {
            it.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY

            })
            it.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Api-Key", API_KEY)
                val request = requestBuilder.build()
                chain.proceed(request)
            }


        } else {
            it
        }
    }
    .build()
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()


interface ApiProject {
    // POST
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>
    @GET("posts/{id}/newer")

    suspend fun getNewerPosts(@Path("id") id: Long): Response<List<Post>>

    @GET("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePostById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    // EVENTS

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/{id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @GET("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun deleteEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<Event>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>
}

object ApiService {
    val service: ApiProject by lazy {
        retrofit.create(ApiProject::class.java)
    }

}