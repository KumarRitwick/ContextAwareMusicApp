package com.example.contextawaremusicapp.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Track

class GenreSectionAdapter(
    private val genreSections: List<GenreSection>,
    private val onTrackClick: (Track) -> Unit // Add this parameter to accept a click listener
) : RecyclerView.Adapter<GenreSectionAdapter.GenreSectionViewHolder>() {

    class GenreSectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val genreTitle: TextView = view.findViewById(R.id.genre_title)
        val tracksRecyclerView: RecyclerView = view.findViewById(R.id.tracks_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_section, parent, false)
        return GenreSectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreSectionViewHolder, position: Int) {
        val genreSection = genreSections[position]
        holder.genreTitle.text = genreSection.genre

        holder.tracksRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        val trackAdapter = TrackAdapter(genreSection.tracks) { track ->
            onTrackClick(track)
        }
        holder.tracksRecyclerView.adapter = trackAdapter
    }

    override fun getItemCount() = genreSections.size
}

data class GenreSection(
    val genre: String,
    val tracks: List<Track>
)
