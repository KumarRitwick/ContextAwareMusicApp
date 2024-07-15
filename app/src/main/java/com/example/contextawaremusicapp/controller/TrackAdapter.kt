package com.example.contextawaremusicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contextawaremusicapp.model.Playlist

class TrackAdapter(private var tracks: List<Playlist>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

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
        holder.trackName.text = track.name
        holder.artistName.text = track.description // Assuming Playlist has a description field
        // Optionally load the album image if you have a URL
    }

    override fun getItemCount() = tracks.size

    fun updateTracks(newTracks: List<Playlist>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
