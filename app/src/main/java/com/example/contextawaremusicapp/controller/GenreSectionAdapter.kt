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
    private val onTrackClick: (Track) -> Unit // Adding the lambda parameter
) : RecyclerView.Adapter<GenreSectionAdapter.GenreSectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_section, parent, false)
        return GenreSectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreSectionViewHolder, position: Int) {
        val genreSection = genreSections[position]
        holder.bind(genreSection)
    }

    override fun getItemCount(): Int = genreSections.size

    inner class GenreSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val genreTitle: TextView = itemView.findViewById(R.id.genre_title)
        private val tracksRecyclerView: RecyclerView = itemView.findViewById(R.id.tracks_recycler_view)

        fun bind(genreSection: GenreSection) {
            genreTitle.text = genreSection.genre
            tracksRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            val trackAdapter = TrackAdapter(genreSection.tracks, onTrackClick)
            tracksRecyclerView.adapter = trackAdapter
        }
    }
}

data class GenreSection(
    val genre: String,
    val tracks: List<Track>
)
