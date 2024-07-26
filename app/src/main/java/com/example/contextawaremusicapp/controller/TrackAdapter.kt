package com.example.contextawaremusicapp.controller

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Track

class TrackAdapter(private var tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trackName: TextView = view.findViewById(R.id.textViewTrackName)
        val artistName: TextView = view.findViewById(R.id.textViewArtistName)
        val albumImage: ImageView = view.findViewById(R.id.imageViewAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        if (track != null) {
            holder.trackName.text = track.name
            holder.artistName.text = track.artists.joinToString(", ") { it.name }
            if (track.album.images.isNotEmpty()) {
                Glide.with(holder.albumImage.context)
                    .load(track.album.images[0].url)
                    .into(holder.albumImage)
            } else {
                holder.albumImage.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            Log.e("TrackAdapter", "Track at position $position is null")
            holder.trackName.text = "Unknown"
            holder.artistName.text = "Unknown Artist"
            holder.albumImage.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount() = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
