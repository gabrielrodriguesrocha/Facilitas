package com.ufscar.sor.dcomp.facilitas

//import android.support.test.InstrumentationRegistry.getArguments
import android.app.Notification
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import com.couchbase.lite.internal.support.Log
import java.net.URI
import java.net.URISyntaxException

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.ufscar.sor.dcomp.facilitas.activity.MainActivity
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v4.app.NotificationCompat
import net.danlew.android.joda.JodaTimeAndroid
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.Context
import android.app.NotificationChannel
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import com.couchbase.lite.*
import com.couchbase.lite.Authenticator
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ufscar.sor.dcomp.facilitas.activity.LoadingActivity
import com.ufscar.sor.dcomp.facilitas.activity.LoginActivity
import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.net.URL
import java.util.*


class Application : android.app.Application(), ReplicatorChangeListener {

    private var DATABASE_NAME: String = DEFAULT_DATABASE_NAME
    private var SYNCGATEWAY_URL: String = DEFAULT_SYNCGATEWAY_URL
    private var SYNC_ENABLED: Boolean = true

    private var database: Database? = null
    private var replicator: Replicator? = null
    private var username: String = DEFAULT_DATABASE_NAME

    private var databaseCRUD: DatabaseCRUD? = null

    private val backup: Database? = null
    private val backupReplicator: Replicator? = null

    private var singleton: Application? = null

    private val httpClient = OkHttpClient()
    private val gson = Gson()



    override fun onCreate() {
        super.onCreate()

        val settings = getDefaultSharedPreferences(this)

        JodaTimeAndroid.init(this)

        DATABASE_NAME = settings.getString("databaseName", DEFAULT_DATABASE_NAME)
        if (settings.getString("sgUrl", "") == "") {
            with(settings.edit()) {
                putString("sgUrl", DEFAULT_SYNCGATEWAY_URL)
                apply()
            }
        }
        SYNCGATEWAY_URL = settings.getString("sgUrl", DEFAULT_SYNCGATEWAY_URL)
        SYNC_ENABLED = settings.getBoolean("syncEnabled", SYNC_ENABLED)
        username = settings.getString("googleAccount", DEFAULT_DATABASE_NAME)
        if (settings.getString("databaseGroup", "") == "") {
            with(settings.edit()) {
                putString("databaseGroup", UUID.randomUUID().toString())
                apply()
            }
        }

        singleton = this

        //authenticate()
    }

    fun authenticate() {
        val settings = getDefaultSharedPreferences(this)

        val expiryTime = settings.getLong("SyncGatewaySessionExpiry", -1)
        val systemTime = System.currentTimeMillis()
        if (expiryTime != -1L && expiryTime > systemTime
            || !isNetworkAvailable()) {
            startSession(username, settings.getString("SyncGatewaySession", ""))
            return
        }

        if (!isSignedIn() && GoogleSignIn.getLastSignedInAccount(this)!!.idToken != null) {
                val task = GoogleSignIn.getLastSignedInAccount(this)
                loginWithGoogleSignIn(task!!.idToken!!)
        }

        if(!isSignedIn()
                || GoogleSignIn.getLastSignedInAccount(this)?.isExpired == true
                || GoogleSignIn.getLastSignedInAccount(this)!!.idToken == null) {
            val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
            signInClient.revokeAccess().addOnCompleteListener {
                startLoginActivity()
            }
        }
    }

    fun loginWithGoogleSignIn(idToken: String) {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        // Send POST _session with the idToken to create a new SGW session:
        val url = settings.getString("sgUrl", SYNCGATEWAY_URL)
        val request = Request.Builder()
                .url(URL("http://" + url + "_session"))
                .header("Authorization", "Bearer $idToken")
                .post(FormBody.Builder().build())
                .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //Log.w("Failed to create a new SGW session with IDToken : $idToken", e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val type = object : TypeToken<Map<String, Any>>() {

                    }.type
                    val session = gson.fromJson(response.body()!!.charStream(), type) as Map<String, Any>
                    val userInfo = session["userCtx"] as Map<String, Any>
                    val cookies = Cookie.parseAll(HttpUrl.get(URL("http://" + settings.getString("sgUrl", SYNCGATEWAY_URL) + "_session"))!!, response.headers())
                    val sessionCookie = cookies.find { cookie -> cookie.name() == "SyncGatewaySession" }
                    username = userInfo["name"].toString()
                    with(settings.edit()) {
                        putString("SyncGatewaySession", sessionCookie!!.value())
                        putLong("SyncGatewaySessionExpiry", sessionCookie.expiresAt())
                        putString("googleAccount", username)
                        apply()
                    }
                    startSession(username, sessionCookie!!.value())
                }
            }
        })
    }

    private fun isNetworkAvailable() : Boolean {
        val connectivityManager = getSystemService (Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun completeLogin() {
        runOnUiThread(Runnable {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })
    }

    fun startLoginActivity() {
        runOnUiThread(Runnable {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })
    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }
    override fun onTerminate() {
        closeDatabase()
        super.onTerminate()
    }

    fun getDatabase(): Database? {
        return database
    }

    fun getUsername(): String? {
        return username
    }

    fun getCrud(): DatabaseCRUD? {
        return databaseCRUD
    }

    // -------------------------
    // Session/Login/Logout
    // -------------------------
    private fun startSessionLocally(username: String, password: String?) {
        openDatabase(username)
        this.username = username
        startReplicationLocally(username, password)
        databaseCRUD = DatabaseCRUD(username, database!!)

        //localBackup(username)

        // TODO: After authenticated, move to next screen
        completeLogin()
    }

    private fun startSession(username: String, sessionId: String) {
        val settings = getDefaultSharedPreferences(this)
        openDatabase(username)
        this.username = username
        startReplication(username, sessionId)
        databaseCRUD = DatabaseCRUD(username, database!!)

        //localBackup(username)
        if (settings.getString("authId", "") == "") {
            val authId = databaseCRUD!!.saveAuthentication(name = username, group = settings.getString("databaseGroup", "test"))?.id
            with(settings.edit()) {
                putString("authId", authId)
                apply()
            }
        }

        // TODO: After authenticated, move to next screen
        completeLogin()
    }


    // -------------------------
    // Database operation
    // -------------------------

    private fun openDatabase(dbname: String) {
        val config = DatabaseConfiguration(applicationContext)
        try {
            database = Database(dbname, config)
        } catch (e: CouchbaseLiteException) {
            //Log.e(TAG, "Failed to create Database instance: %s - %s", e, dbname, config)
            // TODO: error handling
        }
    }

    private fun closeDatabase() {
        if (database != null) {
            try {
                database!!.close()
            } catch (e: CouchbaseLiteException) {
                //Log.e(TAG, "Failed to close Database", e)
                // TODO: error handling
            }

        }
    }

    private fun createDatabaseIndex() {

    }

    // -------------------------
    // Replicator operation
    // -------------------------
    private fun startReplicationLocally(username: String?, password: String?) {
        //if (!SYNC_ENABLED) return

        val uri: URI
        val settings = getDefaultSharedPreferences(this)
        try {
            uri = URI(settings.getString("sgUrl", SYNCGATEWAY_URL))
        } catch (e: URISyntaxException) {
            //Log.e(TAG, "Failed parse URI: %s", e, DEFAULT_SYNCGATEWAY_URL)
            return
        }

        val endpoint = URLEndpoint(uri)
        val config = ReplicatorConfiguration(database, endpoint)
                .setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL)
                .setContinuous(true)

        // authentication
        if (username != null && password != null)
            config.authenticator = BasicAuthenticator(username, password)

        replicator = Replicator(config)
        replicator!!.addChangeListener(this)
        replicator!!.start()
    }

    private fun startReplication(username: String?, sessionId: String) {
        //if (!SYNC_ENABLED) return

        val uri: URI
        val settings = getDefaultSharedPreferences(this)
        try {
            uri = URI("ws://" + settings.getString("sgUrl", SYNCGATEWAY_URL))
        } catch (e: URISyntaxException) {
            //Log.e(TAG, "Failed parse URI: %s", e, DEFAULT_SYNCGATEWAY_URL)
            return
        }

        val endpoint = URLEndpoint(uri)
        val config = ReplicatorConfiguration(database, endpoint)
                .setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL)
                .setContinuous(true)

        // authentication
        //if (username != null)
        config.authenticator = SessionAuthenticator(sessionId)

        replicator = Replicator(config)
        replicator!!.addChangeListener(this)
        replicator!!.start()
    }

    private fun stopReplication() {
        //if (!SYNC_ENABLED) return

        replicator!!.stop()
    }

    private fun runOnUiThread(runnable: Runnable) {
        Handler(applicationContext.mainLooper).post(runnable)
    }


    // --------------------------------------------------
    // ReplicatorChangeListener implementation
    // --------------------------------------------------
    override fun changed(change: ReplicatorChange) {
        //Log.i(TAG, "[Todo] Replicator: status -> %s", change.status)
        if (change.status.error != null && change.status.error.code == 401) {
            Toast.makeText(applicationContext, "Authentication Error: Your username or password is not correct.", Toast.LENGTH_LONG).show()
            //logout()
        }
        /*val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX)

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_clipboard_clear)
                .setTicker("Hearty365")
                //     .setPriority(Notification.PRIORITY_MAX)
        notificationBuilder.setContentTitle("Mudanças nos dados!")
                .setContentText("Verifique o aplicativo para checar as mudanças.")
                .setContentInfo("Info")
        if (change.status.progress.completed == change.status.progress.total)
            notificationManager.notify(/*notification id*/1, notificationBuilder.build())*/
    }

    companion object {

        internal val TAG = Application::class.java.simpleName

        private const val DEFAULT_SYNCGATEWAY_URL = "ec2-18-231-198-169.sa-east-1.compute.amazonaws.com:4984/db/"
        private const val DEFAULT_DATABASE_NAME = "db"

        private const val PREFS_NAME = "SETTINGS"
    }
}