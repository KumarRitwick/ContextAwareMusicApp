package com.example.contextawaremusicapp.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Playlist

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onItemClick: (String) -> Unit // Change to accept only playlist URI
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val playlistDescription: TextView = view.findViewById(R.id.playlistDescription)
        val playlistImage: ImageView = view.findViewById(R.id.playlistImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name
        holder.playlistDescription.text = playlist.description
        if (playlist.images.isNotEmpty()) {
            Glide.with(holder.playlistImage.context)
                .load(playlist.images[0].url)
                .into(holder.playlistImage)
        }
        holder.itemView.setOnClickListener {
            onItemClick(playlist.uri)
        }
    }

    override fun getItemCount() = playlists.size

    fun updateTracks(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
