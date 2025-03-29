package com.example.jobhub.config

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createRetrofit(): Retrofit {
        return retrofit
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}