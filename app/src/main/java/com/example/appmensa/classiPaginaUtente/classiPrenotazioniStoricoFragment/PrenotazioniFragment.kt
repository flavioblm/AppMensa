package com.example.appmensa.classiPaginaUtente.classiPrenotazioniStoricoFragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.ApiService
import com.example.appmensa.retrofit.CreaPrenotazioneRequest
import com.example.appmensa.retrofit.Feedback
import com.example.appmensa.retrofit.FeedbackResponse
import com.example.appmensa.retrofit.MenuDettaglioResponse
import com.example.appmensa.retrofit.Piatto
import com.example.appmensa.retrofit.PrenotazioneResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PrenotazioniFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PrenotazioniAdapter
    private lateinit var apiService: ApiService
    private lateinit var piatti: List<Piatto>
    private val piattiPerCategoria = mutableMapOf<String, List<Piatto>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prenotazioni, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPrenotazioni)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inizializza Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Carica le prenotazioni
        loadPrenotazioni()

        // Set up FloatingActionButton to show dialog
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton2)
        fab.setOnClickListener {
            showAddMenuDialog()
        }
        return view
    }

    private fun loadPrenotazioni() {
        // Assumiamo che l'ID dell'utente sia 1, sostituisci con l'ID effettivo
        val userId = getUserIdFromSharedPreferences()

        apiService.getPrenotazioniUtente(userId).enqueue(object : Callback<List<PrenotazioneResponse>> {
            override fun onResponse(call: Call<List<PrenotazioneResponse>>, response: Response<List<PrenotazioneResponse>>) {
                if (response.isSuccessful) {
                    val prenotazioni = response.body()?.filter { it.stato != "consumato" } ?: emptyList()
                    Log.d("Prenotazione", "ID: ${prenotazioni}")
                    adapter = PrenotazioniAdapter(prenotazioni)
                    recyclerView.adapter = adapter
                    loadMenuDetails(prenotazioni)
                } else {
                    Toast.makeText(context, "Errore nel caricamento delle prenotazioni", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PrenotazioneResponse>>, t: Throwable) {
                Toast.makeText(context, "Errore di rete", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMenuDetails(prenotazioni: List<PrenotazioneResponse>) {
        prenotazioni.forEach { prenotazione ->

            apiService.getMenuDettaglio(prenotazione.id_menu).enqueue(object : Callback<List<MenuDettaglioResponse>> {
                override fun onResponse(call: Call<List<MenuDettaglioResponse>>, response: Response<List<MenuDettaglioResponse>>) {
                    if (response.isSuccessful) {
                        val menuDettagli = response.body() ?: emptyList()
                        adapter.updateMenuDettagli(prenotazione.id, menuDettagli)
                    }
                }

                override fun onFailure(call: Call<List<MenuDettaglioResponse>>, t: Throwable) {
                    // Gestisci l'errore se necessario
                }
            })
        }
    }

    private fun showAddMenuDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_prenotazione, null)
        val antipastoSpinner: Spinner = dialogView.findViewById(R.id.spinnerAntipasto)
        val primoSpinner: Spinner = dialogView.findViewById(R.id.spinnerPrimo)
        val secondoSpinner: Spinner = dialogView.findViewById(R.id.spinnerSecondo)
        val dolceSpinner: Spinner = dialogView.findViewById(R.id.spinnerDolce)

        loadPiatti {
            activity?.runOnUiThread {
                setupSpinner(antipastoSpinner, "Antipasto")
                setupSpinner(primoSpinner, "Primo")
                setupSpinner(secondoSpinner, "Secondo")
                setupSpinner(dolceSpinner, "Dolce")

                AlertDialog.Builder(requireContext())
                    .setTitle("Aggiungi Prenotazione")
                    .setView(dialogView)
                    .setPositiveButton("Prenota") { dialog, _ ->
                        val antipasto = piattiPerCategoria["Antipasto"]?.get(antipastoSpinner.selectedItemPosition)
                        val primo = piattiPerCategoria["Primo"]?.get(primoSpinner.selectedItemPosition)
                        val secondo = piattiPerCategoria["Secondo"]?.get(secondoSpinner.selectedItemPosition)
                        val dolce = piattiPerCategoria["Dolce"]?.get(dolceSpinner.selectedItemPosition)

                        if (antipasto != null && primo != null && secondo != null && dolce != null) {
                            val userId = getUserIdFromSharedPreferences()
                            val request = CreaPrenotazioneRequest(
                                id_utente = userId,
                                id_antipasto = antipasto.id,
                                id_primo = primo.id,
                                id_secondo = secondo.id,
                                id_dolce = dolce.id
                            )

                            apiService.creaPrenotazione(request).enqueue(object : Callback<PrenotazioneResponse> {
                                override fun onResponse(call: Call<PrenotazioneResponse>, response: Response<PrenotazioneResponse>) {
                                    if (response.isSuccessful) {
                                        val prenotazioneResponse = response.body()
                                        Toast.makeText(context, "Prenotazione creata con successo", Toast.LENGTH_SHORT).show()
                                        loadPrenotazioni() // Aggiorna la lista delle prenotazioni
                                    } else {
                                        Toast.makeText(context, "Errore nella creazione della prenotazione: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onFailure(call: Call<PrenotazioneResponse>, t: Throwable) {
                                    Toast.makeText(context, "Errore di rete: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                        } else {
                            Toast.makeText(context, "Seleziona tutti i piatti per creare la prenotazione", Toast.LENGTH_SHORT).show()
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    private fun loadPiatti(callback: () -> Unit) {
        val categories = listOf("Antipasto", "Primo", "Secondo", "Dolce")
        var completedRequests = 0

        categories.forEach { category ->
            apiService.getPiattiByCategoria(category).enqueue(object : Callback<List<Piatto>> {
                override fun onResponse(call: Call<List<Piatto>>, response: Response<List<Piatto>>) {
                    if (response.isSuccessful) {
                        piattiPerCategoria[category] = response.body() ?: emptyList()
                    } else {
                        Toast.makeText(context, "Errore nel caricamento dei piatti di categoria $category", Toast.LENGTH_SHORT).show()
                    }
                    completedRequests++
                    if (completedRequests == categories.size) {
                        // Tutte le richieste sono state completate
                        callback()
                    }
                }

                override fun onFailure(call: Call<List<Piatto>>, t: Throwable) {
                    Toast.makeText(context, "Errore di rete per la categoria $category", Toast.LENGTH_SHORT).show()
                    completedRequests++
                    if (completedRequests == categories.size) {
                        // Tutte le richieste sono state completate
                        callback()
                    }
                }
            })
        }
    }

    private fun setupSpinner(spinner: Spinner, category: String) {
        val piatti = piattiPerCategoria[category] ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, piatti.map { it.nome })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // Aggiungi al back stack se necessario
            .commit()
    }

    private fun getUserIdFromSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("userId", -1) // -1 è il valore di default se l'ID non viene trovato
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
            holder.buttonValuta.visibility = View.GONE
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
