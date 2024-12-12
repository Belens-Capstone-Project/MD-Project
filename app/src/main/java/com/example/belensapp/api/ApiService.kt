package com.example.belensapp.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict")
    fun predict(
        @Part file: MultipartBody.Part
    ): Call<PredictResponse>

    @GET("history")
    fun getHistory(@Header("Authorization") token: String): Call<List<PredictResponse>>

    @GET("news")
    fun getNews(@Header("Authorization") authorization: String): Call<List<NewsResponseItem>>
}

