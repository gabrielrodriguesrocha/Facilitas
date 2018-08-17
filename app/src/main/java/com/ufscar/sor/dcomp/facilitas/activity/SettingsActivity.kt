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
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.CustomFragmentPagerAdapter
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.support.constraint.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import kotlin.math.sign


class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val group = findViewById<EditText>(R.id.group)
        val url = findViewById<EditText>(R.id.url)

        val settings = PreferenceManager.getDefaultSharedPreferences(this)

        findViewById<Button>(R.id.cancel).setOnClickListener(this)
        findViewById<Button>(R.id.save_settings).setOnClickListener(this)
        findViewById<Button>(R.id.signOut).setOnClickListener(this)
        findViewById<Button>(R.id.revokeAccess).setOnClickListener(this)


        group.setText(settings.getString("databaseGroup", ""))
        url.setText(settings.getString("sgUrl", ""))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel -> { setResult(RESULT_CANCELED); super.onBackPressed() }
            R.id.save_settings -> saveSettings()
            R.id.signOut -> signOut()
            R.id.revokeAccess -> revokeAccess()
        }
    }

    private fun saveSettings() {
        val group = findViewById<EditText>(R.id.group)
        val url = findViewById<EditText>(R.id.url)
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val databaseCRUD = (application as Application).getCrud()
        with (settings.edit()) {
            putString("databaseGroup", group.text.toString())
            putString("sgUrl", url.text.toString())
            apply()
        }
        /*val doc = */databaseCRUD!!.saveAuthentication(docId = settings.getString("authId", ""), name = (application as Application).getUsername()!!, group = settings.getString("databaseGroup", "test"))
        //val channels = doc!!.getString("channels")
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    private fun signOut() {
        val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        signInClient.signOut().addOnCompleteListener {
            (application as Application).startLoginActivity()
        }
    }

    private fun revokeAccess() {
        val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        signInClient.revokeAccess().addOnCompleteListener {
            (application as Application).startLoginActivity()
        }
    }
}