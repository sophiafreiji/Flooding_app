package com.example.flood.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class PredictionRequest(
    val temperature: Float,
    val humidity: Float,
    val soil_moisture: Float,
    val water_level: Float,
    val rainfall: Float
)

data class PredictionResponse(
    val prediction: Int
)

data class SensorDataResponse(
    val data: List<SensorData>
)

data class SensorData(
    val device_id: String,
    val temperature: Double,
    val humidity: Double,
    val soil_moisture: Double,
    val water_level: Double,
    val rainfall: Double,
    val timestamp: String
)

interface FloodAPI {
    @POST("predict")
    fun predictFlood(@Body request: PredictionRequest): Call<PredictionResponse>

    @GET("api/data")
    fun getSensorData(): Call<SensorDataResponse>
}