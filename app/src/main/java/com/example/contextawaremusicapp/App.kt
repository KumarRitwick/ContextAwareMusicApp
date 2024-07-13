package com.example.contextawaremusicapp

import android.app.Application
import com.example.contextawaremusicapp.model.SpotifyApi

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SpotifyApi.init(this)
    }
}
