package com.example.appmensa.classiPaginaUtente

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.R.id.bottom_nav
import com.example.appmensa.classiMainActivity.MainActivity
import com.example.appmensa.classiPaginaUtente.drawerMenuAdapters.ChatAdapter
import com.example.appmensa.classiPaginaUtente.drawerMenuAdapters.FeedbackAdapter
import com.example.appmensa.classiPaginaUtente.drawerMenuAdapters.NotificaAdapter
import com.example.appmensa.retrofit.Chat
import com.example.appmensa.retrofit.FeedbackResponse
import com.example.appmensa.retrofit.Messaggio
import com.example.appmensa.retrofit.NotificaResponse
import com.example.appmensa.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UtenteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPreferences: SharedPreferences
    private var currentChat: Chat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utente)


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Inizializzazione della Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Debug per verificare i valori salvati
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Inizializzazione della DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Configurazione della AppBar per gestire il drawer menu
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.fragment_principale,
            R.id.fragment_prenotazioni,
            R.id.fragment_storico,
            R.id.fragment_news,
            // Aggiungi qui tutti i fragment che vuoi trattare come destinazioni di livello superiore
        ).setDrawerLayout(drawerLayout)
            .build()

        // Collegamento del NavController alla BottomNavigationView
        setupBottomNavigationView()

        // Collegamento del NavController al DrawerLayout e NavigationView
        setupNavigationDrawer()

        // Impostazione della toolbar per supportare il navigateUp()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    // Funzione per configurare la BottomNavigationView con NavController
    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(bottom_nav)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)


    }

    // Funzione per configurare il DrawerLayout con NavController e NavigationView
    private fun setupNavigationDrawer() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        NavigationUI.setupWithNavController(navView, navController)
        navView.setNavigationItemSelectedListener(this)

        // Ottieni il riferimento alla TextView tv_username nel nav_header.xml
        val headerView = navView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.tv_username)

        // Recupera il nome e cognome salvati nelle SharedPreferences
        val nome = sharedPreferences.getString("nome", "")
        val cognome = sharedPreferences.getString("cognome", "")
        // Costruisci il testo da visualizzare
        val nomeCognome = "$nome $cognome"
        tvUsername.text = nomeCognome
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.visualizza_recensioni -> {
                showFeedbackDialog()
                return true
            }
            R.id.visualizza_notifiche -> {
                showNotificheDialog()
                return true
            }
            R.id.nav_modifica_account -> {
                showUpdatePasswordDialog()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.nav_contatta_assistenza -> {
                showAssistanceChat()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
        }
        return false
    }

    private fun showUpdatePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_modifica, null)
        val editTextNewPassword = dialogView.findViewById<EditText>(R.id.editTextTextPassword)
        val editTextOldPassword = dialogView.findViewById<EditText>(R.id.editTextTextPassword2)
        val buttonSave = dialogView.findViewById<Button>(R.id.button2)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        buttonSave.setOnClickListener {
            val newPassword = editTextNewPassword.text.toString()
            val oldPassword = editTextOldPassword.text.toString()

            if (newPassword.isEmpty() || oldPassword.isEmpty()) {
                Toast.makeText(this, "Per favore, riempi tutti i campi", Toast.LENGTH_SHORT).show()
            } else {
                updateUserPassword(newPassword, oldPassword)
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }
    private fun showNotificheDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_notifiche)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.notificheRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        RetrofitClient.retrofit.getNotifiche().enqueue(object : Callback<List<NotificaResponse>> {
            override fun onResponse(call: Call<List<NotificaResponse>>, response: Response<List<NotificaResponse>>) {
                if (response.isSuccessful) {
                    val notifiche = response.body() ?: emptyList()
                    val adapter = NotificaAdapter(notifiche)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@UtenteActivity, "Impossibile recuperare le notifiche", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NotificaResponse>>, t: Throwable) {
                Toast.makeText(this@UtenteActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show()
    }
    private fun updateUserPassword(newPassword: String, oldPassword: String) {
        // Recupera l'ID dell'utente dalle SharedPreferences o da un'altra fonte
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "ID utente non trovato", Toast.LENGTH_SHORT).show()
            return
        }

        // Crea la richiesta per aggiornare la password
        val passwordUpdateRequest = mapOf(
            "id" to userId.toString(),
            "newPassword" to newPassword,
            "oldPassword" to oldPassword
        )

        // Esegui la chiamata API per aggiornare la password
        RetrofitClient.retrofit.updatePassword(userId, passwordUpdateRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UtenteActivity, "Password aggiornata con successo", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@UtenteActivity, "Errore nell'aggiornamento della password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UtenteActivity, "Errore di rete", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showFeedbackDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_feedbacks)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.feedbackRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        RetrofitClient.retrofit.getFeedbacks().enqueue(object : Callback<List<FeedbackResponse>> {
            override fun onResponse(call: Call<List<FeedbackResponse>>, response: Response<List<FeedbackResponse>>) {
                if (response.isSuccessful) {
                    val feedbacks = response.body() ?: emptyList()
                    val adapter = FeedbackAdapter(feedbacks)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@UtenteActivity, "Failed to fetch feedbacks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<FeedbackResponse>>, t: Throwable) {
                Toast.makeText(this@UtenteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show()
    }
    private fun showAssistanceChat() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_chat)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerViewChat)
        val editText = dialog.findViewById<EditText>(R.id.editTextMessage)
        val sendButton = dialog.findViewById<ImageButton>(R.id.imageButton)

        val messageList = mutableListOf<Messaggio>()
        val adapter = ChatAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Fa iniziare la lista dal basso
        }

        val userId = sharedPreferences.getInt("userId", -1)

        // Prima, cerchiamo una chat esistente per l'utente
        RetrofitClient.retrofit.getChats().enqueue(object : Callback<List<Chat>> {
            override fun onResponse(call: Call<List<Chat>>, response: Response<List<Chat>>) {
                if (response.isSuccessful) {
                    val existingChat = response.body()?.find { it.id_utente == userId }
                    if (existingChat != null) {
                        // Chat esistente trovata, la usiamo
                        currentChat = existingChat
                        loadMessages(currentChat!!.id, adapter, messageList)
                    } else {
                        // Nessuna chat esistente, ne creiamo una nuova
                        createNewChat(userId, adapter, messageList)
                    }
                } else {
                    // Gestire l'errore
                }
            }

            override fun onFailure(call: Call<List<Chat>>, t: Throwable) {
                // Gestire l'errore
            }
        })

        sendButton.setOnClickListener {
            val messageText = editText.text.toString().trim()
            if (messageText.isNotEmpty() && currentChat != null) {
                val newMessage = Messaggio(0, messageText, getCurrentDateTime(), currentChat!!.id, userId, null)
                sendMessage(newMessage, adapter, messageList)
                editText.text.clear()
            }
        }

        dialog.show()
    }

    private fun createNewChat(userId: Int, adapter: ChatAdapter, messageList: MutableList<Messaggio>) {
        RetrofitClient.retrofit.createChat(Chat(0, userId, null, "aperto")).enqueue(object : Callback<Chat> {
            override fun onResponse(call: Call<Chat>, response: Response<Chat>) {
                if (response.isSuccessful) {
                    currentChat = response.body()
                    loadMessages(currentChat!!.id, adapter, messageList)
                } else {
                    // Gestire l'errore

                }
            }

            override fun onFailure(call: Call<Chat>, t: Throwable) {
                // Gestire l'errore

            }
        })
    }

    private fun loadMessages(chatId: Int, adapter: ChatAdapter, messageList: MutableList<Messaggio>) {

        RetrofitClient.retrofit.getMessaggi(chatId).enqueue(object : Callback<List<Messaggio>> {
            override fun onResponse(call: Call<List<Messaggio>>, response: Response<List<Messaggio>>) {
                if (response.isSuccessful) {
                    messageList.clear()
                    messageList.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    // Gestire l'errore
                    Toast.makeText(this@UtenteActivity, "Errore nel caricamento dei messaggi", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<List<Messaggio>>, t: Throwable) {
                // Gestire l'errore
            }
        })
    }

    private fun sendMessage(message: Messaggio, adapter: ChatAdapter, messageList: MutableList<Messaggio>) {
        RetrofitClient.retrofit.createMessaggio(message).enqueue(object : Callback<Messaggio> {
            override fun onResponse(call: Call<Messaggio>, response: Response<Messaggio>) {
                if (response.isSuccessful) {
                    messageList.add(response.body()!!)
                    adapter.notifyItemInserted(messageList.size - 1)
                } else {
                    // Gestire l'errore
                }
            }

            override fun onFailure(call: Call<Messaggio>, t: Throwable) {
                // Gestire l'errore
            }
        })
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }



    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
