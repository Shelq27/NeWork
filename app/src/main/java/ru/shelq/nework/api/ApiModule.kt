package ru.shelq.nework.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.shelq.nework.BuildConfig
import ru.shelq.nework.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
        private const val API_KEY = BuildConfig.API_KEY
    }


    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY

        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .let {
            if (BuildConfig.DEBUG) {

                it.addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("Api-Key", API_KEY)
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                it.addInterceptor { chain ->
                    appAuth.authState.value.token?.let { token ->
                        val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", token)
                            .build()
                        return@addInterceptor chain.proceed(newRequest)
                    }
                    chain.proceed(chain.request())
                }

            } else {
                it
            }
        }
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    @Provides
    @Singleton
    fun providePostApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create()
}

