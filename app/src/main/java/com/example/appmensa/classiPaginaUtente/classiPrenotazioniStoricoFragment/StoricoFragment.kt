package com.example.appmensa.classiPaginaUtente.classiPrenotazioniStoricoFragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.ApiService
import com.example.appmensa.retrofit.Feedback
import com.example.appmensa.retrofit.FeedbackResponse
import com.example.appmensa.retrofit.MenuDettaglioResponse
import com.example.appmensa.retrofit.PrenotazioneResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class StoricoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PrenotazioniAdapter
    private lateinit var apiService: ApiService
    private lateinit var textViewCarboidrati: TextView
    private lateinit var textViewGrassi: TextView
    private lateinit var textViewProteine: TextView
    private lateinit var textViewPastiConsumati: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_storico, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewStorico)
        recyclerView.layoutManager = LinearLayoutManager(context)
        textViewCarboidrati = view.findViewById(R.id.textViewCarboidrati)
        textViewGrassi = view.findViewById(R.id.textViewGrassi)
        textViewProteine = view.findViewById(R.id.textViewProteine)
        textViewPastiConsumati = view.findViewById(R.id.textViewPastiConsumati)

        // Inizializza Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Carica le prenotazioni consumate
        loadStorico()

        return view
    }

    private fun loadStorico() {
        val userId = getUserIdFromSharedPreferences()

        apiService.getPrenotazioniUtente(userId).enqueue(object : Callback<List<PrenotazioneResponse>> {
            override fun onResponse(call: Call<List<PrenotazioneResponse>>, response: Response<List<PrenotazioneResponse>>) {
                if (response.isSuccessful) {
                    val prenotazioni = response.body()?.filter { it.stato == "consumato" } ?: emptyList()
                    adapter = PrenotazioniAdapter(prenotazioni)
                    recyclerView.adapter = adapter
                    textViewPastiConsumati.text = "Pasti consumati: ${prenotazioni.size}"
                    calculateNutritionalReport(prenotazioni)
                } else {
                    Toast.makeText(context, "Errore nel caricamento delle prenotazioni", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PrenotazioneResponse>>, t: Throwable) {
                Toast.makeText(context, "Errore di rete", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateNutritionalReport(prenotazioni: List<PrenotazioneResponse>) {
        var totalCarboidrati = 0.0
        var totalGrassi = 0.0
        var totalProteine = 0.0
        var count = 0

        prenotazioni.forEach { prenotazione ->
            apiService.getMenuDettaglio(prenotazione.id_menu).enqueue(object : Callback<List<MenuDettaglioResponse>> {
                override fun onResponse(call: Call<List<MenuDettaglioResponse>>, response: Response<List<MenuDettaglioResponse>>) {
                    if (response.isSuccessful) {
                        val dettagli = response.body() ?: emptyList()
                        adapter.updateMenuDettagli(prenotazione.id,dettagli)
                        dettagli.forEach { dettaglio ->
                            totalCarboidrati += dettaglio.Carboidrati
                            totalGrassi += dettaglio.Grassi
                            totalProteine += dettaglio.Proteine
                        }
                        count++
                        if (count == prenotazioni.size) {
                            val avgCarboidrati = totalCarboidrati / count
                            val avgGrassi = totalGrassi / count
                            val avgProteine = totalProteine / count

                            textViewCarboidrati.text = "Media Carboidrati: %.2f".format(avgCarboidrati)
                            textViewGrassi.text = "Media Grassi: %.2f".format(avgGrassi)
                            textViewProteine.text = "Media Proteine: %.2f".format(avgProteine)
                        }
                    }
                }

                override fun onFailure(call: Call<List<MenuDettaglioResponse>>, t: Throwable) {
                    // Gestisci l'errore se necessario
                }
            })
        }
    }

    private fun getUserIdFromSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("userId", -1)
    }

    inner class PrenotazioniAdapter(private var prenotazioni: List<PrenotazioneResponse>) :
        RecyclerView.Adapter<PrenotazioniAdapter.PrenotazioneViewHolder>() {

        private val menuDettagli = mutableMapOf<Int, List<MenuDettaglioResponse>>()

        inner class PrenotazioneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewData: TextView = itemView.findViewById(R.id.textViewData)
            val textViewStato: TextView = itemView.findViewById(R.id.textViewStato)
            val textViewMenu: TextView = itemView.findViewById(R.id.textViewMenu)
            val buttonValuta: Button =  itemView.findViewById(R.id.button3)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrenotazioneViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenotazione, parent, false)
            return PrenotazioneViewHolder(view)
        }

        override fun onBindViewHolder(holder: PrenotazioneViewHolder, position: Int) {
            val prenotazione = prenotazioni[position]
            holder.textViewData.text = prenotazione.data_prenotazione
            holder.textViewStato.text = prenotazione.stato

            val dettagli = menuDettagli[prenotazione.id]
            if (dettagli != null) {
                val menuText = dettagli.joinToString("\n") { "${it.Tipo}: ${it.Nome}" }
                holder.textViewMenu.text = menuText
            } else {
                holder.textViewMenu.text = "Caricamento menu..."
            }
            holder.buttonValuta.setOnClickListener {
                showValutazioneDialog(prenotazione.id_menu)
            }
        }

        private fun showValutazioneDialog(idMenu: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_valutazione, null)
            val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)
            val editTextCommento: EditText = dialogView.findViewById(R.id.editTextCommento)
            val buttonSubmit: Button = dialogView.findViewById(R.id.buttonSubmit)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            buttonSubmit.setOnClickListener {
                val valutazione = ratingBar.rating.toInt()
                val commento = editTextCommento.text.toString()
                val idUtente = getUserIdFromSharedPreferences()

                val feedback = Feedback(
                    commento = commento,
                    valutazione = valutazione,
                    id_utente = idUtente,
                    id_menu = idMenu
                )

                apiService.createFeedback(feedback).enqueue(object : Callback<FeedbackResponse> {
                    override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                        if (response.isSuccessful && response.body()?.error == null) {
                            Toast.makeText(context, "Feedback inviato con successo", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "Errore nell'invio del feedback", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                        Toast.makeText(context, "Errore di rete", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            dialog.show()
        }

        override fun getItemCount() = prenotazioni.size

        fun updatePrenotazioni(newPrenotazioni: List<PrenotazioneResponse>) {
            prenotazioni = newPrenotazioni.filter { it.stato != "consumato" }
            notifyDataSetChanged()
        }

        fun updateMenuDettagli(prenotazioneId: Int, dettagli: List<MenuDettaglioResponse>) {
            menuDettagli[prenotazioneId] = dettagli
            notifyDataSetChanged()
        }
    }
}
