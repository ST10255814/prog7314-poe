package com.example.rentwise.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.rentwise.PrivacyPolicyFragment
import com.example.rentwise.R
import com.example.rentwise.databinding.FragmentMainSettingsBinding
import com.example.rentwise.faq.FAQChatBot

class MainSettingsFragment : Fragment() {
    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(layoutInflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtonListeners()
        prepareLanguageSpinner()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun prepareLanguageSpinner() {
        val languages = resources.getStringArray(R.array.language_options)

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.custom_spinner_item,
            languages
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = getItem(position)
                return view
            }
        }
        binding.languageSpinner.adapter = adapter
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonListeners(){
        binding.helpAndSupportTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        binding.helpAndSupportTab.setOnClickListener {
            val intent = Intent(requireContext(), FAQChatBot::class.java)
            startActivity(intent)
        }
        binding.aboutTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        binding.privacyPolicyTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        binding.privacyPolicyTab.setOnClickListener {
            commitFragmentToContainer(PrivacyPolicyFragment())
        }
    }
    private fun commitFragmentToContainer(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}