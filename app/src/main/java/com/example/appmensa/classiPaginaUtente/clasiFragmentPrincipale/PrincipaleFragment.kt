package com.example.appmensa.classiPaginaUtente.clasiFragmentPrincipale
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.retrofit.MenuGiornaliero
import com.example.appmensa.R
import com.example.appmensa.R.id.recycler_view
import com.example.appmensa.retrofit.ApiService
import com.example.appmensa.retrofit.MenuDettaglioResponse
import com.example.appmensa.retrofit.Piatto
import com.example.appmensa.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PrincipaleFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val menuList = mutableListOf<MenuGiornaliero>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_principale, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchMenuGiornaliero()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        menuAdapter = MenuAdapter(menuList,requireContext())
        recyclerView.adapter = menuAdapter
    }

    private fun fetchMenuGiornaliero() {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val calendar = Calendar.getInstance()

        // Formato per l'API (yyyy-MM-dd)
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Formato per la visualizzazione in italiano (dd/MM/yyyy)
        val italianDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
        val today = Calendar.getInstance()
        for (i in 0..10) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val apiDate = apiDateFormat.format(today.time)
            val displayDate = italianDateFormat.format(today.time)

            api.getMenuDettaglio(apiDate).enqueue(object : Callback<List<MenuDettaglioResponse>> {
                override fun onResponse(call: Call<List<MenuDettaglioResponse>>, response: Response<List<MenuDettaglioResponse>>) {
                    if (response.isSuccessful) {
                        val menuDettaglio = response.body()
                        if (menuDettaglio != null) {
                            val menu = createMenuFromDettaglio(menuDettaglio, displayDate)
                            menuList.add(menu)
                            menuList.sortBy { italianDateFormat.parse(it.data) }
                            menuAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onFailure(call: Call<List<MenuDettaglioResponse>>, t: Throwable) {
                    Log.e("API", "Errore nella chiamata API", t)
                }
            })

            today.add(Calendar.DAY_OF_YEAR,1)
        }
    }

    private fun createMenuFromDettaglio(menuDettaglio: List<MenuDettaglioResponse>, formattedDate: String): MenuGiornaliero {
        var antipasto: Piatto? = null
        var primo: Piatto? = null
        var secondo: Piatto? = null
        var dolce: Piatto? = null

        for (item in menuDettaglio) {
            val piatto = Piatto(item.ID_Piatto, item.Nome, item.Descrizione, item.Tipo, item.Calorie, item.Carboidrati, item.Grassi, item.Proteine)
            when (item.Tipo.toLowerCase()) {
                "antipasto" -> antipasto = piatto
                "primo" -> primo = piatto
                "secondo" -> secondo = piatto
                "dolce" -> dolce = piatto
            }
        }

        return MenuGiornaliero(
            menuDettaglio.firstOrNull()?.ID_Menu ?: 0,
            "Menu del $formattedDate",
            formattedDate,
            antipasto ?: Piatto(0, "", "", "", 0.0, 0.0, 0.0, 0.0),
            primo ?: Piatto(0, "", "", "", 0.0, 0.0, 0.0, 0.0),
            secondo ?: Piatto(0, "", "", "", 0.0, 0.0, 0.0, 0.0),
            dolce ?: Piatto(0, "", "", "", 0.0, 0.0, 0.0, 0.0)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuList.clear()
    }
}

