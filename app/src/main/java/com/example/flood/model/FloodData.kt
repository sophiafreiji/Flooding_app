package com.example.flood.model

data class FloodData(
    val water_level: Int,
    val rainfall: Int,
    val soil_moisture: Int,
    val humidity: Int,
    val temperature: Int,
    val weather_forecast: WeatherForecast
)

data class WeatherForecast(
    val temp: Int,
    val humidity: Int,
    val rain: Int,
    val pressure: Int,
    val prediction: String,
    val confidence: Int
)