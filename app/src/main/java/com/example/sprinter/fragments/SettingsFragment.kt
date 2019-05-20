package com.example.sprinter.fragments

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.preference.*
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val keyLanguagePreference = "LanguagePreferences"
        const val keyUnitsPreference = "UnitsPreferences"
        const val keyEnableIndianPreference = "ModePreferenceSwitch"
        const val keyRunTimeIndianPreference = "RunTimeSeekBarPreference"
        const val keyWalkTimeIndianPreference = "WalkTimeSeekBarPreference"

        fun setLanguage(lang : String, activity : Activity, context : Context?) {
            val locale = Locale(lang)
            val config = Configuration()
            Locale.setDefault(locale)
            config.setLocale(locale)
            activity.resources?.updateConfiguration(config, context?.resources?.displayMetrics)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val languagePref = findPreference<Preference>(keyLanguagePreference)
        val unitsPref = findPreference<Preference>(keyUnitsPreference)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val enableIndianRunPref = findPreference<Preference>(keyEnableIndianPreference)
        val runTimeIndianPref = findPreference<Preference>(keyRunTimeIndianPreference)
        val walkTimeIndianPref = findPreference<Preference>(keyWalkTimeIndianPreference)

        bindSummaryValue(languagePref!!)
        bindSummaryValue(unitsPref!!)
        bindSummaryValue(enableIndianRunPref!!)

        languagePref.summary = sharedPreferences.getString(keyLanguagePreference, "Čeština")
        unitsPref.summary = sharedPreferences.getString(keyUnitsPreference, "Km")
        runTimeIndianPref?.isEnabled = sharedPreferences.getBoolean(keyEnableIndianPreference, false)
        walkTimeIndianPref?.isEnabled = sharedPreferences.getBoolean(keyEnableIndianPreference, false)



        activity?.title = getString(R.string.settings)

    }

    private val preferenceListener = Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
        val stringValue : String = newValue.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            if (preference.key == keyUnitsPreference && index >= 0) {
                MainActivity.units = preference.entryValues[index].toString()
                preference.setSummary(if (index >= 0) preference.entryValues[index] else null)
            }

            if (preference.key == keyLanguagePreference && index >= 0) {
                val value = preference.entryValues[index]
                var loc = "en"
                if (value == "Čeština") {
                    loc = "cs"
                }
                Log.d("LOCALE", loc)
                setLanguage(loc, activity as Activity, context)
                preference.setSummary(if (index >= 0) preference.entryValues[index] else null)
                fragmentManager?.beginTransaction()?.replace(R.id.settingsFragment, SettingsFragment())?.commit()
            }
        }
        if (preference is SwitchPreference) {
            val runTimePreference = findPreference<Preference>(keyRunTimeIndianPreference)
            runTimePreference?.isEnabled = stringValue == "true"
            val walkTimePreference = findPreference<Preference>(keyWalkTimeIndianPreference)
            walkTimePreference?.isEnabled = stringValue == "true"

        }
        true
    }

    private fun bindSummaryValue(preference : Preference) {
        preference.onPreferenceChangeListener = preferenceListener
        preferenceListener.onPreferenceChange(preference, PreferenceManager
            .getDefaultSharedPreferences(activity))
    }
}