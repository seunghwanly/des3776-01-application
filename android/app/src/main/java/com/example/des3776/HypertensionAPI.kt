package com.example.des3776

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HyperTensionAPI {
    @GET("hypertensions")
    fun getEvaluation(
        @Query("index") index: Int
    ): Call<JsonObject>
}