package com.example.contextawaremusicapp.ui.home

import Audiobook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R

class AudiobookAdapter(
    private var audiobooks: List<Audiobook>,
    private val onClick: (Audiobook) -> Unit
) : RecyclerView.Adapter<AudiobookAdapter.AudiobookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiobookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audiobook, parent, false)
        return AudiobookViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudiobookViewHolder, position: Int) {
        val audiobook = audiobooks[position]
        if (audiobook != null) {
            holder.bind(audiobook)
        }
    }

    override fun getItemCount(): Int = audiobooks.size

    fun updateAudiobooks(newAudiobooks: List<Audiobook>) {
        audiobooks = newAudiobooks.filterNotNull()
        notifyDataSetChanged()
    }

    inner class AudiobookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.audiobook_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.audiobook_author)
        private val coverImageView: ImageView = itemView.findViewById(R.id.audiobook_cover)

        fun bind(audiobook: Audiobook) {
            titleTextView.text = audiobook.name ?: "Unknown Title"
            authorTextView.text = audiobook.authors.joinToString { it.name } ?: "Unknown Author"
            if (audiobook.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(audiobook.images[0].url)
                    .into(coverImageView)
            } else {
                coverImageView.setImageResource(R.drawable.placeholder_image)
            }
            itemView.setOnClickListener { onClick(audiobook) }
        }
    }
}
