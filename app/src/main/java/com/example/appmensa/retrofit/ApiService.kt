package com.example.appmensa.retrofit
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    @PUT("pwm/utente/{id}/password")
    fun updatePassword(@Path("id") id: Int, @Body passwordUpdateRequest: Map<String, String>): Call<Void>

    @POST("pwm/utente")
    fun insertUtente(@Body body: JsonObject): Call<JsonObject>

    @POST("pwm/login")
    fun loginUser(@Body loginRequest: Map<String, String>): Call<LoginResponse>

    @GET("pwm/menu_dettaglio")
    fun getMenuDettaglio(@Query("data_menu") dataMenu: String): Call<List<MenuDettaglioResponse>>

    @POST("pwm/prenotazione")
    fun createPrenotazione(@Body request: PrenotazioneRequest): Call<PrenotazioneResponse>

    @GET("pwm/prenotazioni/{id_utente}")
    fun getPrenotazioniUtente(@Path("id_utente") idUtente: Int): Call<List<PrenotazioneResponse>>

    @GET("pwm/menu_dettaglio/{id_menu}")
    fun getMenuDettaglio(@Path("id_menu") idMenu: Int): Call<List<MenuDettaglioResponse>>


    @GET("pwm/menu/options/{category}")
    fun getMenuOptions(@Path("category") category: String): Call<List<String>>

    @POST("pwm/menu/aggiungi")
    fun createMenu(@Body menuGiornaliero: MenuGiornaliero): Call<MenuGiornaliero>

    @GET("pwm/piatti/categoria/{categoria}")
    fun getPiattiByCategoria(@Path("categoria") categoria: String): Call<List<Piatto>>

    @POST("pwm/crea_prenotazione")
    fun creaPrenotazione(@Body request: CreaPrenotazioneRequest): Call<PrenotazioneResponse>

    @POST("pwm/feedback")
    fun createFeedback(@Body feedback: Feedback): Call<FeedbackResponse>

    @GET("pwm/eventi")
    fun getEventi(): Call<List<News>>


    @GET("pwm/notifiche")
    fun getNotifiche(): Call<List<NotificaResponse>>

    @GET("pwm/chats")
    fun getChats(): Call<List<Chat>>


    @POST("pwm/chat")
    fun createChat(@Body chat: Chat): Call<Chat>


    @GET("pwm/messaggi")
    fun getMessaggi(@Query("chat_id") chatId: Int): Call<List<Messaggio>>

    @POST("pwm/messaggio")
    fun createMessaggio(@Body messaggio: Messaggio): Call<Messaggio>

    @GET("pwm/feedbacks")
    fun getFeedbacks(): Call<List<FeedbackResponse>>

    companion object {
        const val BASE_URL = "http://192.168.214.110:9000"
    }
}

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val id: Int? = null,
    val nome: String? = null,
    val cognome: String? = null,
    val email: String? = null,
    val ruolo: String? = null
)

data class Utente (
    val id: Int? = null,
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val dataNascita: String,
    val ruolo: String,
)

data class Piatto(
    val id: Int,
    val nome: String,
    val descrizione: String,
    val tipo: String,
    val calorie: Double,
    val carboidrati: Double,
    val grassi: Double,
    val proteine: Double
)


data class MenuGiornaliero(
    val id: Int,
    val nome: String,
    val data: String,
    val antipasto: Piatto,
    val primo: Piatto,
    val secondo: Piatto,
    val dolce: Piatto
)

data class MenuDettaglioResponse(
    val ID_Menu: Int,
    val ID_Piatto: Int,
    val Nome: String,
    val Tipo: String,
    val Descrizione: String,
    val Calorie: Double,
    val Carboidrati: Double,
    val Grassi: Double,
    val Proteine: Double
)

data class Prenotazione(
    val id: Int,
    val data: String,
    val stato: String,
    val idMenu: Int
)

data class PrenotazioneConDettagli(
    val prenotazione: Prenotazione,
    val dettaglioMenu: MenuDettaglioResponse
)

data class PrenotazioneRequest(
    val data_prenotazione: String,
    val id_utente: Int,
    val id_menu: Int,
    val stato: String
)

data class PrenotazioneResponse(
    val id: Int,
    val data_prenotazione: String,
    val id_utente: Int,
    val id_menu: Int,
    val stato: String
)
data class CreaPrenotazioneRequest(
    val id_utente: Int,
    val id_antipasto: Int,
    val id_primo: Int,
    val id_secondo: Int,
    val id_dolce: Int
)
data class Feedback(
    val commento: String,
    val valutazione: Int,
    val id_utente: Int,
    val id_menu: Int
)

data class FeedbackResponse(
    val id: Int,
    val commento: String,
    val valutazione: Int,
    val id_utente: Int,
    val id_menu: Int,
    val error: String?
)

data class News(
    val id: Int,
    val titolo: String,
    val descrizione: String,
    val dataEvento: String,
    val urlImmagine: String
)
data class Chat(
    val id: Int,
    val id_utente: Int,
    val dataApertura: String?,
    val stato: String
)

data class Messaggio(
    val id: Int,
    val testo: String,
    val data_messaggio: String,
    val id_chat: Int,
    val id_utente: Int?,
    val id_gestore: Int?
)

data class NotificaResponse(
    val id: Int,
    val titolo: String,
    val messaggio: String,
    val data_notifica: String
)