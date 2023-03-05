package com.example.agrithonapp

import java.io.Serializable

data class ImageResponse(
    val description: String,
    val prediction: String,
    val source: String,
    val symptoms: String
): Serializable