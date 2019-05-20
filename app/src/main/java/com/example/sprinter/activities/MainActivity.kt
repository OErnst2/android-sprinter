package com.example.sprinter.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.sprinter.R
import com.example.sprinter.databases.StaticData
import com.example.sprinter.databases.TracksDb
import com.example.sprinter.fragments.SettingsFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    var mLocationPermissionGranted = false
    val ERROR_DIALOG_REQUEST = 9001
    val PERMISSIONS_REQUEST_ENABLE_GPS = 9002
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003

    lateinit var data: TracksDb

    companion object {
        var units : String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StaticData.prepareDatabaseAndTables(this)
        val baseActivity = Intent(this, BaseActivity::class.java)
        startActivity(baseActivity)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        units = sharedPreferences.getString(SettingsFragment.keyUnitsPreference, "Km")

        val language = sharedPreferences.getString(SettingsFragment.keyLanguagePreference, "English")

        if (language == "English") {
            SettingsFragment.setLanguage("en", this, this)
        } else {
            SettingsFragment.setLanguage("cs", this, this)
        }

        finish()
    }

    private fun setData() {
        for (x in data) {
            text.text = text.text.toString() + x.name + "\n"
        }
    }
}
