package com.example.contextawaremusicapp.ui.songdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R

class SongDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_detail, container, false)

        val trackName = "Track Name"
        val trackArtist = "Track Artist"
        val albumArtUrl = "Album Art URL"

        val trackNameTextView = view.findViewById<TextView>(R.id.track_name)
        val trackArtistTextView = view.findViewById<TextView>(R.id.track_artist)
        val albumArtImageView = view.findViewById<ImageView>(R.id.album_art)

        trackNameTextView.text = trackName
        trackArtistTextView.text = trackArtist

        Glide.with(this)
            .load(albumArtUrl)
            .into(albumArtImageView)

        return view
    }
}
