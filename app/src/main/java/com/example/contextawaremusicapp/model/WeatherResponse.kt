package com.example.contextawaremusicapp.model

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather?,
    val daily: DailyWeather?
)

data class CurrentWeather(
    val time: String,
    val interval: Int,
    val precipitation: Double,
    val rain: Double,
    val showers: Double,
    val snowfall: Double,
    val weather_code: Int
)

data class DailyWeather(
    val time: List<String>,
    val weather_code: List<Int>
)
