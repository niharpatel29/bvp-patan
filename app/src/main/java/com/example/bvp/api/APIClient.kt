package com.example.bvp.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

fun postClient(): Retrofit? {
    val baseURL = "https://www.rockingbharat.com/bvp_patan/"
    var retrofit: Retrofit? = null

    val httpLoggingInterceptor = HttpLoggingInterceptor()

    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient()
        .newBuilder()
        .connectTimeout(80000, TimeUnit.SECONDS)
        .readTimeout(80000, TimeUnit.SECONDS)
        .writeTimeout(80000, TimeUnit.SECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .build()

    if (retrofit == null) {
        retrofit = Retrofit
            .Builder()
            .baseUrl(baseURL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .create()
                )
            ).build()
    }
    return retrofit
}