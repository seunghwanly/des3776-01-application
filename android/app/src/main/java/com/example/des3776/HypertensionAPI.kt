package com.example.des3776

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface HyperTensionAPI {
    @GET("hypertensions")
    fun getEvaluation(
        @Query("index") index: Int
    ): Call<JsonObject>

    @Multipart
    @POST("hypertensions")
    fun getResultFromFile(
        @Part body: MultipartBody.Part,
    ): Call<JsonObject>
}