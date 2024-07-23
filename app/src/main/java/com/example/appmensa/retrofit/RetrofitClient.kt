package com.example.appmensa.retrofit

import com.example.appmensa.retrofit.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.appmensa.retrofit.ApiService.Companion.BASE_URL
object RetrofitClient {



        val retrofit: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

}