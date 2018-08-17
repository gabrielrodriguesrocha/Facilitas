package com.ufscar.sor.dcomp.facilitas.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.ufscar.sor.dcomp.facilitas.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.privacyPolicy).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/gabrielrodriguesrocha/Facilitas/blob/master/PRIVACY.md")))
        }
    }
}