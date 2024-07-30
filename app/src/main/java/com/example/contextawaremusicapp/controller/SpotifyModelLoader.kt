package com.example.contextawaremusicapp.controller

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelCache
import com.bumptech.glide.load.data.HttpUrlFetcher
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import com.example.contextawaremusicapp.model.SpotifyImage
import java.io.InputStream

class SpotifyModelLoader(private val modelCache: ModelCache<SpotifyImage, GlideUrl>) : ModelLoader<SpotifyImage, InputStream> {

    override fun buildLoadData(model: SpotifyImage, width: Int, height: Int, options: Options): LoadData<InputStream>? {
        val glideUrl = modelCache[model, 0, 0] ?: GlideUrl("https://i.scdn.co/image/${model.uri.split(":").last()}").also {
            modelCache.put(model, 0, 0, it)
        }
        return LoadData(ObjectKey(model), HttpUrlFetcher(glideUrl, 2500))
    }

    override fun handles(model: SpotifyImage): Boolean {
        return model.uri.startsWith("spotify:image")
    }

    class Factory : ModelLoaderFactory<SpotifyImage, InputStream> {
        private val modelCache = ModelCache<SpotifyImage, GlideUrl>(500)

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SpotifyImage, InputStream> {
            return SpotifyModelLoader(modelCache)
        }

        override fun teardown() {
            // Do nothing.
        }
    }
}
