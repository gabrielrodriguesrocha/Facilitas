package com.ufscar.sor.dcomp.facilitas.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R


class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private var googleApiClient: GoogleApiClient? = null

    private var shouldContinueLogoutFromGoogleSignIn: Boolean = false

    private var logInWithGoogleSignIn: Boolean
        get() {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            return sharedPref.getBoolean(USE_GOOGLE_SIGN_IN_KEY, false)
        }
        set(used) {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(USE_GOOGLE_SIGN_IN_KEY, used)
            editor.apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        val googleSignInButton = findViewById<SignInButton>(R.id.googleSignInButton)
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD)
        //googleSignInButton.setScopes(gso.scopeArray)
        googleSignInButton.setOnClickListener(this)

        val action = intent.action
        if (INTENT_ACTION_LOGOUT == action) {
            logout()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.googleSignInButton -> googleSignIn()
        }
    }

    private fun googleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleGoogleSignInResult(result)
        }
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult) {
        var success = false
        var errorMessage: String? = null

        if (result.isSuccess) {
            val acct = result.signInAccount
            val idToken = acct!!.idToken
            if (idToken != null) {
                val app = application as Application
                app.loginWithGoogleSignIn(idToken)
                success = true
            } else
                errorMessage = "Google Sign-in failed : No ID Token returned"

            logInWithGoogleSignIn = true
        } else
            errorMessage = "Google Sign-in failed: (" +
                    result.status.statusCode + ") " +
                    result.status.statusMessage

        if (!success) {
            //val application = application as Application
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
        }
    }

    /** logout  */
    private fun logout() {
        if (logInWithGoogleSignIn)
            logoutFromGoogleSignIn()
        else {
            clearWebViewCookies()
            completeLogout()
        }
    }

    private fun clearWebViewCookies() {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
        } else {
            //cookieManager.removeAllCookie()
            //CookieSyncManager.getInstance().sync()
        }
    }

    private fun completeLogout() {
        //val application = application as Application
        Toast.makeText(this, "Logout successfully", Toast.LENGTH_LONG)
    }

    private fun logoutFromGoogleSignIn() {
        if (googleApiClient!!.isConnected) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(object : ResultCallback<Status> {
                internal var application = getApplication() as Application
                override fun onResult(status: Status) {
                    if (status.isSuccess) {
                        clearWebViewCookies()
                        logInWithGoogleSignIn = false
                        completeLogout()
                    } //else
                        //Toast.makeText(this, "Failed to sign out from Google Signin", Toast.LENGTH_SHORT)
                }
            })
        } else {
            shouldContinueLogoutFromGoogleSignIn = true
            if (!googleApiClient!!.isConnecting)
                googleApiClient!!.connect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        if (shouldContinueLogoutFromGoogleSignIn)
            logoutFromGoogleSignIn()
        shouldContinueLogoutFromGoogleSignIn = false
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        shouldContinueLogoutFromGoogleSignIn = false

        val errorMessage = "Google Sign-in connection failed: (" +
                result.errorCode + ") " +
                result.errorMessage
        //val application = application as Application
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
    }

    override fun onConnectionSuspended(i: Int) {
        // Do nothing
    }

    companion object {

        val INTENT_ACTION_LOGOUT = "logout"

        private val RC_GOOGLE_SIGN_IN = 9001

        private val USE_GOOGLE_SIGN_IN_KEY = "UseGoogleSignIn"
    }
}
