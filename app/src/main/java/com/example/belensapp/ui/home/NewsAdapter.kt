package com.example.belensapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.api.NewsResponseItem


class NewsAdapter(
    private val newsList: List<NewsResponseItem>,
    private val onItemClick: (NewsResponseItem) -> Unit // Listener for item clicks
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.card_image)
        val textView: TextView = view.findViewById(R.id.card_text)

        init {
            // Handle item click
            itemView.setOnClickListener {
                onItemClick(newsList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]

        // Ensure the image URL is not null or empty before loading it with Glide
        if (!newsItem.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(newsItem.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_camera)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_image)
        }

        // Set the title text
        holder.textView.text = newsItem.title
    }

    override fun getItemCount(): Int = newsList.size
}

