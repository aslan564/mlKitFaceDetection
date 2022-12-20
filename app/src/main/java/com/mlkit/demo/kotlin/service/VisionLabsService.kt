package com.mlkit.demo.kotlin.service

import com.mlkit.demo.model.VisionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VisionLabsService {

    @Multipart
    @POST("events")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<String>

}