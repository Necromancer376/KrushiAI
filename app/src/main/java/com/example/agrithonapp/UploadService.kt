package com.example.agrithonapp

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    @Multipart
    @POST("/")
    suspend fun uploadImageAdmin(
        @Part image: MultipartBody.Part
    ): ImageResponse

    @Multipart
    @POST("/predict")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): NewAPIResponse


    @POST("/text")
    suspend fun uploadVoice(
        @Body body: Map<String, String>
    ): NewAPIResponse
}