package com.example.appmensa.classiMainActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appmensa.classiPaginaUtente.UtenteActivity
import com.example.appmensa.databinding.FragmentLoginBinding
import com.example.appmensa.retrofit.LoginResponse
import com.example.appmensa.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        val emailEditText: EditText = binding.editTextEmail
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(inputText).matches() && inputText.isNotEmpty()) {
                    emailEditText.error = "Formato email non valido"
                }
            }
        })

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPswd.text.toString()

            val loginRequest = mapOf("email" to email, "password" to password)
            loginUser(loginRequest)
        }

        binding.showSignUp.setOnClickListener {
            (activity as MainActivity).showSignupFragment()
        }

        return view
    }

    private fun loginUser(loginRequest: Map<String, String>) {
        val apiService = RetrofitClient.retrofit

        apiService.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()
                    saveUserDetails(loginResponse)



                    Toast.makeText(requireContext(), "Login effettuato con successo!", Toast.LENGTH_SHORT).show()
                    // Gestisci il risultato della login (es. naviga alla schermata successiva)
                    val intent = Intent(requireContext(), UtenteActivity::class.java)
                    startActivity(intent)
                    // Chiudi l'attuale fragment o activity se necessario
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Credenziali non valide", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserDetails(loginResponse: LoginResponse?) {
        val sharedPreferences = requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Salva i dettagli dell'utente se il login è riuscito
        loginResponse?.let {
            editor.putInt("userId", it.id ?: -1)
            editor.putString("nome", it.nome ?: "")
            editor.putString("cognome", it.cognome ?: "")
            editor.putString("email", it.email ?: "")
            editor.putString("userRole", it.ruolo ?: "")
        }

        editor.apply()

    }

}

