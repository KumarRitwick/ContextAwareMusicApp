package com.example.contextawaremusicapp.controller

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.contextawaremusicapp.model.SpotifyImage
import java.io.InputStream

@GlideModule
class SpotifyGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(SpotifyImage::class.java, InputStream::class.java, SpotifyModelLoader.Factory())
    }
}
