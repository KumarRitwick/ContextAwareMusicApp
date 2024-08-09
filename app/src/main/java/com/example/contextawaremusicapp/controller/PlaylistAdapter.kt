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

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_PLAYLIST = 1

class PlaylistAdapter(
    private var items: List<Any>,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val playlistImage: ImageView = view.findViewById(R.id.playlistImage)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_PLAYLIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
            PlaylistViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.headerTitle.text = items[position] as String
        } else if (holder is PlaylistViewHolder) {
            val playlist = items[position] as Playlist
            holder.playlistName.text = playlist.name
            if (playlist.images.isNotEmpty()) {
                Glide.with(holder.playlistImage.context)
                    .load(playlist.images[0].url)
                    .into(holder.playlistImage)
            } else {
                holder.playlistImage.setImageResource(R.drawable.placeholder_image)
            }

            holder.itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        items = newPlaylists
        notifyDataSetChanged()
    }

    fun addHeaderAndPlaylists(header: String, playlists: List<Playlist>) {
        val updatedItems = items.toMutableList()
        updatedItems.add(header)
        updatedItems.addAll(playlists)
        items = updatedItems
        notifyDataSetChanged()
    }
}
