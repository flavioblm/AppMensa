package com.example.appmensa.classiPaginaUtente.classiNewsFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmensa.R
import com.example.appmensa.retrofit.News

class NewsAdapter(private val newsList: List<News>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titoloNews: TextView = itemView.findViewById(R.id.titoloNews)
        val dataNews: TextView = itemView.findViewById(R.id.dataNews)
        val descrizioneNews: TextView = itemView.findViewById(R.id.descrizioneNews)
        val immagineNews: ImageView = itemView.findViewById(R.id.immagineNews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.titoloNews.text = news.titolo
        holder.dataNews.text = news.dataEvento
        holder.descrizioneNews.text = news.descrizione

        // Carica l'immagine dall'URL usando Glide
        Glide.with(holder.itemView.context)
            .load(news.urlImmagine)
            .placeholder(R.drawable.aranciosublu) // immagine di placeholder mentre si carica
            .error(R.drawable.cuoco) // immagine da mostrare in caso di errore
            .into(holder.immagineNews)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }
}