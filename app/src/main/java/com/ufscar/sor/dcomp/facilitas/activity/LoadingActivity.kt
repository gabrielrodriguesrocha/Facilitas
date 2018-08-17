package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import android.graphics.Bitmap



class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        /*val app = application as Application
        Thread(Runnable {
            app.authenticate()
        }).start()*/
    }

    override fun onResume() {
        super.onResume()

        val app = application as Application
        Thread(Runnable {
            app.authenticate()
        }).start()
    }
}