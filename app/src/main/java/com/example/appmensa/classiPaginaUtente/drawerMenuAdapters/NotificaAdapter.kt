package com.example.appmensa.classiPaginaUtente.drawerMenuAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.NotificaResponse

class NotificaAdapter(private val notifiche: List<NotificaResponse>) :
    RecyclerView.Adapter<NotificaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titoloTextView: TextView = view.findViewById(R.id.titoloTextView)
        val messaggioTextView: TextView = view.findViewById(R.id.messaggioTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notifica, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notifica = notifiche[position]
        holder.titoloTextView.text = notifica.titolo
        holder.messaggioTextView.text = notifica.messaggio
    }

    override fun getItemCount() = notifiche.size
}