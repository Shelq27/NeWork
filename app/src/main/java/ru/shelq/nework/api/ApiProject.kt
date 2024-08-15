package ru.shelq.nework.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.shelq.nework.dto.Post
import ru.shelq.nework.BuildConfig
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
    fun getAllPosts(): Call<List<Post>>

    @GET("posts")
    fun savePost(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun deletePostById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun likePostById(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun dislikePostById(@Path("id") id: Long): Call<Post>
}

object ApiService {
    val service: ApiProject by lazy {
        retrofit.create()
    }

}