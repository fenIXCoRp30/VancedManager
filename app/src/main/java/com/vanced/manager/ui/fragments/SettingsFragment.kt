package com.vanced.manager.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.vanced.manager.R
import com.vanced.manager.adapter.GetNotifAdapter
import com.vanced.manager.core.ui.base.BindingFragment
import com.vanced.manager.core.ui.ext.showDialog
import com.vanced.manager.databinding.FragmentSettingsBinding
import com.vanced.manager.ui.dialogs.*
import com.vanced.manager.utils.Extensions.toHex
import com.vanced.manager.utils.LanguageHelper.getLanguageFormat
import com.vanced.manager.utils.ThemeHelper.accentColor
import com.vanced.manager.utils.ThemeHelper.defAccentColor
import java.io.File

class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    private companion object {
        const val LIGHT = "Light"
        const val DARK = "Dark"
    }

    private val prefs by lazy { getDefaultSharedPreferences(requireActivity()) }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "serviced_sleep_timer" -> {
                binding.servicedTimer.setSummary(sharedPreferences.getInt(key, 1).toString())
            }
        }
    }

    private lateinit var variant: String

    override fun binding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false)

    override fun otherSetups() {
        setHasOptionsMenu(true)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        bindData()
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun bindData() {
        with(binding) {
            variant = prefs.getString("vanced_variant", "nonroot").toString()
            bindRecycler()
            bindFirebase()
            bindManagerVariant()
            bindServiceDTimer()
            bindClearFiles()
            bindManagerTheme()
            bindManagerAccentColor()
            bindManagerLanguage()
            selectApps.setOnClickListener { showDialog(SelectAppsDialog()) }
        }
    }

    private fun FragmentSettingsBinding.bindRecycler() {
        notificationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = GetNotifAdapter(requireActivity())
        }
    }

    private fun FragmentSettingsBinding.bindFirebase() {
        firebase.setOnCheckedListener { _, isChecked ->
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isChecked)
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = isChecked
            FirebaseAnalytics.getInstance(requireActivity()).setAnalyticsCollectionEnabled(isChecked)
        }
    }

    private fun FragmentSettingsBinding.bindManagerVariant() {
        managerVariant.apply {
            setSummary(variant)
            setOnClickListener { showDialog(ManagerVariantDialog()) }
        }
    }

    private fun FragmentSettingsBinding.bindServiceDTimer() {
        servicedTimer.apply {
            if (variant == "root") this.isVisible = true
            setSummary(prefs.getInt("serviced_sleep_timer", 1).toString())
            setOnClickListener { showDialog(ServiceDTimerDialog()) }
        }
    }

    private fun FragmentSettingsBinding.bindClearFiles() {
        clearFiles.setOnClickListener {
            with(requireActivity()) {
                listOf("vanced/nonroot", "vanced/root", "music/nonroot", "music/root", "microg").forEach { dir ->
                    File(getExternalFilesDir(dir)?.path.toString()).deleteRecursively()
                }
                Toast.makeText(this, getString(R.string.cleared_files), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun FragmentSettingsBinding.bindManagerTheme() {
        val themePref = prefs.getString("manager_theme", "System Default")
        managerTheme.apply {
            setSummary(
                when (themePref) {
                    LIGHT -> getString(R.string.theme_light)
                    DARK -> getString(R.string.theme_dark)
                    else -> getString(R.string.system_default)
                }
            )
            setOnClickListener { showDialog(ManagerThemeDialog()) }
        }
    }

    private fun FragmentSettingsBinding.bindManagerAccentColor() {
        managerAccentColor.apply{
            setSummary(prefs.getInt("manager_accent_color", defAccentColor).toHex())
            setOnClickListener { showDialog(ManagerAccentColorDialog()) }
            accentColor.observe(viewLifecycleOwner) {
                setSummary(it.toHex())
            }
        }
    }

    private fun FragmentSettingsBinding.bindManagerLanguage() {
        val langPref = prefs.getString("manager_lang", "System Default")
        managerLanguage.apply {
            setSummary(getLanguageFormat(requireActivity(), requireNotNull(langPref)))
            setOnClickListener { showDialog(ManagerLanguageDialog()) }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val devSettings = getDefaultSharedPreferences(requireActivity()).getBoolean("devSettings", false)
        if (devSettings) {
            inflater.inflate(R.menu.dev_settings_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
}