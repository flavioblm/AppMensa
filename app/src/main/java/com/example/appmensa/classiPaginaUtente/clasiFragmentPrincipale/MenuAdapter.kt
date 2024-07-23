package com.example.appmensa.classiPaginaUtente.clasiFragmentPrincipale

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.MenuGiornaliero
import com.example.appmensa.retrofit.PrenotazioneRequest
import com.example.appmensa.retrofit.PrenotazioneResponse
import com.example.appmensa.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuAdapter(private val menuList: List<MenuGiornaliero> , private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private val VIEW_TYPE_TODAY = 0
    private val VIEW_TYPE_OTHER = 1

    inner class TodayMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMenuDateToday: TextView = itemView.findViewById(R.id.tvMenuDateToday)

        val tvAntipastoNomeToday: TextView = itemView.findViewById(R.id.tvAntipastoNomeToday)
        val tvAntipastoDettagliToday: TextView = itemView.findViewById(R.id.tvAntipastoDettagliToday)

        val tvPrimoNomeToday: TextView = itemView.findViewById(R.id.tvPrimoNomeToday)
        val tvPrimoDettagliToday: TextView = itemView.findViewById(R.id.tvPrimoDettagliToday)

        val tvSecondoNomeToday: TextView = itemView.findViewById(R.id.tvSecondoNomeToday)
        val tvSecondoDettagliToday: TextView = itemView.findViewById(R.id.tvSecondoDettagliToday)

        val tvDolceNomeToday: TextView = itemView.findViewById(R.id.tvDolceNomeToday)
        val tvDolceDettagliToday: TextView = itemView.findViewById(R.id.tvDolceDettagliToday)
        val buttonPrenota : Button = itemView.findViewById(R.id.button)

    }

    inner class OtherMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMenuDate: TextView = itemView.findViewById(R.id.tvMenuDate)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)

        val tvAntipastoNome: TextView = itemView.findViewById(R.id.tvAntipastoNome)
        val tvAntipastoDettagli: TextView = itemView.findViewById(R.id.tvAntipastoDettagli)

        val tvPrimoNome: TextView = itemView.findViewById(R.id.tvPrimoNome)
        val tvPrimoDettagli: TextView = itemView.findViewById(R.id.tvPrimoDettagli)

        val tvSecondoNome: TextView = itemView.findViewById(R.id.tvSecondoNome)
        val tvSecondoDettagli: TextView = itemView.findViewById(R.id.tvSecondoDettagli)

        val tvDolceNome: TextView = itemView.findViewById(R.id.tvDolceNome)
        val tvDolceDettagli: TextView = itemView.findViewById(R.id.tvDolceDettagli)
    }

    override fun getItemViewType(position: Int): Int {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(Date())
        return if (menuList[position].data == today) VIEW_TYPE_TODAY else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TODAY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_menu_today, parent, false)
                TodayMenuViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_menu_expandable, parent, false)
                OtherMenuViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menu = menuList[position]
        when (holder) {
            is TodayMenuViewHolder -> {
                bindTodayMenu(holder, menu)
                holder.buttonPrenota.setOnClickListener{
                    sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                    val userId = sharedPreferences.getInt("userId", -1)
                    val prenotazioneRequest = PrenotazioneRequest(
                        data_prenotazione = menu.data,
                        id_utente = userId,
                        id_menu = menu.id,
                        stato = "confermato"
                    )

                    RetrofitClient.retrofit.createPrenotazione(prenotazioneRequest).enqueue(object : Callback<PrenotazioneResponse> {
                        override fun onResponse(call: Call<PrenotazioneResponse>, response: Response<PrenotazioneResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(holder.itemView.context, "Prenotazione confermata", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(holder.itemView.context, "Errore nella prenotazione", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<PrenotazioneResponse>, t: Throwable) {
                            Toast.makeText(holder.itemView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            is OtherMenuViewHolder -> {
                bindOtherMenu(holder, menu)
            }
        }
    }

    private fun bindTodayMenu(holder: TodayMenuViewHolder, menu: MenuGiornaliero) {
        holder.tvMenuDateToday.text = "Menu di Oggi (${menu.data})"

        // Antipasto
        holder.tvAntipastoNomeToday.text = "Antipasto: ${menu.antipasto.nome}"
        holder.tvAntipastoDettagliToday.text = "Calorie: ${menu.antipasto.calorie}, " +
                "Proteine: ${menu.antipasto.proteine}g, " +
                "Carboidrati: ${menu.antipasto.carboidrati}g, " +
                "Grassi: ${menu.antipasto.grassi}g"

        // Primo
        holder.tvPrimoNomeToday.text = "Primo: ${menu.primo.nome}"
        holder.tvPrimoDettagliToday.text = "Calorie: ${menu.primo.calorie}, " +
                "Proteine: ${menu.primo.proteine}g, " +
                "Carboidrati: ${menu.primo.carboidrati}g, " +
                "Grassi: ${menu.primo.grassi}g"

        // Secondo
        holder.tvSecondoNomeToday.text = "Secondo: ${menu.secondo.nome}"
        holder.tvSecondoDettagliToday.text = "Calorie: ${menu.secondo.calorie}, " +
                "Proteine: ${menu.secondo.proteine}g, " +
                "Carboidrati: ${menu.secondo.carboidrati}g, " +
                "Grassi: ${menu.secondo.grassi}g"

        // Dolce
        holder.tvDolceNomeToday.text = "Dolce: ${menu.dolce.nome}"
        holder.tvDolceDettagliToday.text = "Calorie: ${menu.dolce.calorie}, " +
                "Proteine: ${menu.dolce.proteine}g, " +
                "Carboidrati: ${menu.dolce.carboidrati}g, " +
                "Grassi: ${menu.dolce.grassi}g"

    }

    private fun bindOtherMenu(holder: OtherMenuViewHolder, menu: MenuGiornaliero) {
        holder.tvMenuDate.text = "Menu del ${menu.data}"

        // Antipasto
        holder.tvAntipastoNome.text = "Antipasto: ${menu.antipasto.nome}"
        holder.tvAntipastoDettagli.text = "Calorie: ${menu.antipasto.calorie}, " +
                "Proteine: ${menu.antipasto.proteine}g, " +
                "Carboidrati: ${menu.antipasto.carboidrati}g, " +
                "Grassi: ${menu.antipasto.grassi}g"

        // Primo
        holder.tvPrimoNome.text = "Primo: ${menu.primo.nome}"
        holder.tvPrimoDettagli.text = "Calorie: ${menu.primo.calorie}, " +
                "Proteine: ${menu.primo.proteine}g, " +
                "Carboidrati: ${menu.primo.carboidrati}g, " +
                "Grassi: ${menu.primo.grassi}g"

        // Secondo
        holder.tvSecondoNome.text = "Secondo: ${menu.secondo.nome}"
        holder.tvSecondoDettagli.text = "Calorie: ${menu.secondo.calorie}, " +
                "Proteine: ${menu.secondo.proteine}g, " +
                "Carboidrati: ${menu.secondo.carboidrati}g, " +
                "Grassi: ${menu.secondo.grassi}g"

        // Dolce
        holder.tvDolceNome.text = "Dolce: ${menu.dolce.nome}"
        holder.tvDolceDettagli.text = "Calorie: ${menu.dolce.calorie}, " +
                "Proteine: ${menu.dolce.proteine}g, " +
                "Carboidrati: ${menu.dolce.carboidrati}g, " +
                "Grassi: ${menu.dolce.grassi}g"

        holder.layoutDetails.visibility = View.GONE
        holder.itemView.setOnClickListener {
            holder.layoutDetails.visibility = if (holder.layoutDetails.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    override fun getItemCount() = menuList.size
}