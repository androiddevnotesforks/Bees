package com.iemkol.beez.data.api

import com.iemkol.beez.domain.model.NSFWResponse
import retrofit2.Response
import retrofit2.http.*

interface NSFWApi {
    @Headers(
        "X-RapidAPI-Key: rapid_api_key",
        "X-RapidAPI-Host: nsfw3.p.rapidapi.com"
    )
    @FormUrlEncoded
    @POST("results")
    suspend fun checkNSFWImage(@Field("url") imageUrl:String):Response<NSFWResponse>
}