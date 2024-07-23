package com.example.appmensa.classiMainActivity
import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appmensa.databinding.FragmentSignUpBinding
import com.example.appmensa.retrofit.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText


class SignUpFragment : Fragment() {
    //variabile per gestire il binding
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val emailEditText: EditText = binding.editTextEmaill
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            //controllo formato mail inserito
            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(inputText).matches() && inputText.isNotEmpty()) {
                    emailEditText.error = "Formato email non valido"
                }
            }
        })


        val dateEditText: EditText = binding.editTextDate
        dateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            //controllo formato data inserita
            override fun afterTextChanged(s: Editable?) {
                val datePattern = Regex("\\d{4}-\\d{2}-\\d{2}") // Pattern per yyyy-MM-dd
                val inputText = s.toString()
                if (!datePattern.matches(inputText) && inputText.isNotEmpty()) {
                    dateEditText.error = "Formato data non valido. Usa yyyy-mm-dd"
                }
            }
        })

        binding.buttonSignUp.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val cognome = binding.editTextCognome.text.toString()
            val email = binding.editTextEmaill.text.toString()
            val password = binding.editTextPassword.text.toString()
            val dataNascita = binding.editTextDate.text.toString()
            val ruolo = "user"

            if (nome.isNotEmpty() && cognome.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && dataNascita.isNotEmpty()) {
                val nuovoUtente = JsonObject().apply {
                    addProperty("nome", nome)
                    addProperty("cognome", cognome)
                    addProperty("email", email)
                    addProperty("password", password)
                    addProperty("data_nascita", dataNascita)
                    addProperty("ruolo", ruolo)
                }

                registerUser(nuovoUtente)
            } else {
                Toast.makeText(context, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show()
            }
        }

        binding.showLogin.setOnClickListener {
            (activity as MainActivity).showLoginFragment()
        }

        return binding.root
    }



    private fun registerUser(utente: JsonObject) {

        RetrofitClient.retrofit.insertUtente(utente).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Registrazione effettuata con successo!", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).showLoginFragment()
                } else {
                    Toast.makeText(context, "Errore durante la registrazione", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("RegistrationActivity", "Errore di rete: ${t.message}")
                Toast.makeText(context, "Errore di rete", Toast.LENGTH_SHORT).show()

            }
        })
    }
}
