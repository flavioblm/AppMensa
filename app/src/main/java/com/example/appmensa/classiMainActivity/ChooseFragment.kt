package com.example.appmensa.classiMainActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.appmensa.R

class ChooseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose, container, false)

        view.findViewById<Button>(R.id.buttonAccedi).setOnClickListener {
            (activity as MainActivity).showLoginFragment()
        }

        view.findViewById<Button>(R.id.buttonRegistrati).setOnClickListener {
            (activity as MainActivity).showSignupFragment()
        }

        return view
    }
}