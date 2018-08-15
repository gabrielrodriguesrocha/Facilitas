package com.ufscar.sor.dcomp.facilitas.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.CustomFragmentPagerAdapter

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val group = findViewById<EditText>(R.id.group)
        val url = findViewById<EditText>(R.id.url)

        val settings = PreferenceManager.getDefaultSharedPreferences(this)

        group.setText(settings.getString("databaseGroup", ""))
        url.setText(settings.getString("sgUrl", ""))

        findViewById<Button>(R.id.save_settings).setOnClickListener { v -> saveSettings(v)}
        findViewById<Button>(R.id.cancel).setOnClickListener { _ ->
            setResult(RESULT_CANCELED, null)
            finish()
        }
    }

    private fun saveSettings(v: View) {
        val group = findViewById<EditText>(R.id.group)
        val url = findViewById<EditText>(R.id.url)
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        with (settings.edit()) {
            putString("databaseGroup", group.text.toString())
            putString("sgUrl", url.text.toString())
            apply()
        }
        setResult(RESULT_OK, null)
        finish()
    }
}