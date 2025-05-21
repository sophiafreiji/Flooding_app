package com.example.flood.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class PredictionRequest(
    val temperature: Float,
    val humidity: Float,
    val wind_speed: Float,
    val water_level: Float,
    val rain:Int
)

data class PredictionResponse(
    val prediction: Int
)

data class SensorDataResponse(
    val data: List<SensorData>
)

data class SensorData(
    val temperature: Double,
    val humidity: Double,
    val wind_speed: Double,
    val water_level: Double,
    val rain: Int,
    val timestamp: String
)

interface FloodAPI {
    @POST("predict")
    fun predictFlood(@Body request: PredictionRequest): Call<PredictionResponse>

    @GET("api/data")
    fun getSensorData(): Call<SensorDataResponse>
}

