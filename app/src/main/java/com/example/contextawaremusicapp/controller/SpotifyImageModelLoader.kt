package com.example.contextawaremusicapp.utils

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class SpotifyImageModelLoader(
    modelLoader: ModelLoader<GlideUrl, InputStream>
) : BaseGlideUrlLoader<SpotifyImage>(modelLoader) {

    override fun getUrl(model: SpotifyImage, width: Int, height: Int, options: Options?): String? {
        return "https://i.scdn.co/image/${model.uri.split(":").last()}"
    }

    override fun handles(model: SpotifyImage): Boolean {
        return model.uri.startsWith("spotify:image:")
    }

    class Factory(val context: Context) : ModelLoaderFactory<SpotifyImage, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SpotifyImage, InputStream> {
            val modelLoader = multiFactory.build(GlideUrl::class.java, InputStream::class.java)
            return SpotifyImageModelLoader(modelLoader)
        }

        override fun teardown() {
        }
    }
}
