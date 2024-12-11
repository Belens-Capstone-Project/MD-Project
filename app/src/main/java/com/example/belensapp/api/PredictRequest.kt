package com.example.belensapp.api

import com.google.gson.annotations.SerializedName

data class PredictRequest(
    @SerializedName("image_base64") val imageBase64: String
)
