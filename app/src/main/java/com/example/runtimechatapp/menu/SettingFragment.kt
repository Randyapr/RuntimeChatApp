package com.example.runtimechatapp.menu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.runtimechatapp.R
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate

class SettingFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val themePrefKey = "theme_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inisial SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // apply tema
        val isDarkMode = sharedPreferences.getBoolean(themePrefKey, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val switchTheme: SwitchMaterial = view.findViewById(R.id.switch_theme)


        switchTheme.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }


            with(sharedPreferences.edit()) {
                putBoolean(themePrefKey, isChecked)
                apply()
            }
        }

        val settingImageView: View = view.findViewById(R.id.settingImageView)
        settingImageView.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        return view
    }
}