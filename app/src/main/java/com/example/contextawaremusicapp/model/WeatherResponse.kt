package com.example.contextawaremusicapp.model

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather?,
    val hourly: HourlyWeather
)

data class CurrentWeather(
    val weather_code: Int
)

data class HourlyWeather(
    val time: List<String>,
    val weather_code: List<Int>
)

