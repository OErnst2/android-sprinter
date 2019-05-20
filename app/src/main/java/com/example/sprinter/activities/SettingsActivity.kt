package com.example.sprinter.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sprinter.R
import com.example.sprinter.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction().replace(R.id.settingsFragment, SettingsFragment()).commit()

    }
}
