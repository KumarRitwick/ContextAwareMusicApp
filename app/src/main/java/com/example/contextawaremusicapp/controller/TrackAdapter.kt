package com.example.contextawaremusicapp.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Track

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trackName: TextView = view.findViewById(R.id.trackName)
        val artistName: TextView = view.findViewById(R.id.artistName)
        val albumImage: ImageView = view.findViewById(R.id.albumImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.trackName.text = track.name
        holder.artistName.text = track.artists.joinToString(separator = ", ") { it.name }
        if (track.album.images.isNotEmpty()) {
            Glide.with(holder.albumImage.context)
                .load(track.album.images[0].url)
                .into(holder.albumImage)
        } else {
            holder.albumImage.setImageResource(R.drawable.placeholder_image)
        }

        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    override fun getItemCount() = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
