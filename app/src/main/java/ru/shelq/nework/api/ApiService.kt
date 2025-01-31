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
import ru.shelq.nework.dto.EventApi
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.Post
import ru.shelq.nework.dto.PostApi
import ru.shelq.nework.dto.User


interface ApiService {
    // POST
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getPostLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getPostBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getPostAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: PostApi): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePostById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    // EVENTS

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/latest")
    suspend fun getEventLatest(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getEventAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getEventBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: EventApi): Response<Event>

    @DELETE("events/{id}")
    suspend fun deleteEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<Event>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun notParticipateEventById(@Path("id") id: Long): Response<Event>

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

    //User
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    //WAll
    @GET("{authorId}/wall")
    suspend fun getUserWall(@Path("authorId") id: Long): Response<List<Post>>

    @GET("{authorId}/wall/latest")
    suspend fun getUserWallLatest(
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{authorId}/wall/{id}/newer")
    suspend fun getUserWallNewer(
        @Path("authorId") authorId: Long,
        @Path("id") id: Long
    ): Response<List<Post>>

    @GET("{authorId}/wall/{id}/before")
    suspend fun getUserWallBefore(
        @Path("authorId") authorId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{authorId}/wall/{id}/after")
    suspend fun getUserWallAfter(
        @Path("authorId") authorId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("my/wall")
    suspend fun getMyWall(): Response<List<Post>>

    @GET("my/wall/latest")
    suspend fun getMyWallLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("my/wall/{id}/newer")
    suspend fun getMyWallNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("my/wall/{id}/before")
    suspend fun getMyWallBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("my/wall/{id}/after")
    suspend fun getMyWallAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @POST("{authorId}/wall/{id}/likes")
    suspend fun likeUserPostById(
        @Path("authorId") authorId: Long,
        @Path("id") id: Long
    ): Response<Post>

    @DELETE("{authorId}/wall/{id}/likes")
    suspend fun dislikeUserPostById(
        @Path("authorId") authorId: Long,
        @Path("id") id: Long
    ): Response<Post>

    // Jobs
    @GET("{authorId}/jobs")
    suspend fun getUserJobs(@Path("authorId") id: Long): Response<List<Jobs>>

    @POST("my/jobs")
    suspend fun saveJob(@Body jobs: Jobs): Response<Jobs>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") jobId: Long): Response<Unit>


}

