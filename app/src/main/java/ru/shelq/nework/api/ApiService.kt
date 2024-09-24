package ru.shelq.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.shelq.nework.auth.AuthState
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post


interface ApiService {
    // POST
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Long): Response<List<Post>>


    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

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

    // Reg/Auth

            //Auth
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthState>
        //Reg No Avatar
    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthState>
        //Reg Avatar
        @Multipart
        @POST("users/registration")
        suspend fun registerUserAvatar(
            @Part("login") login: RequestBody,
            @Part("pass") pass: RequestBody,
            @Part("name") name: RequestBody,
            @Part media: MultipartBody.Part
        ): Response<AuthState>


}

