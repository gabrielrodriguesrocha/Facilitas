package com.ufscar.sor.dcomp.facilitas

//import android.support.test.InstrumentationRegistry.getArguments
import android.content.Intent
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.ReplicatorChange
import com.couchbase.lite.ReplicatorChangeListener
import com.couchbase.lite.Replicator
import com.couchbase.lite.ReplicatorConfiguration
import com.couchbase.lite.URLEndpoint
import android.os.Handler
import android.widget.Toast
import com.couchbase.lite.BasicAuthenticator
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.internal.support.Log
import java.net.URI
import java.net.URISyntaxException

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.ufscar.sor.dcomp.facilitas.activity.MainActivity
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD

class Application : android.app.Application(), ReplicatorChangeListener {

    private var database: Database? = null
    private var replicator: Replicator? = null
    private var username: String? = DATABASE_NAME

    private var databaseCRUD: DatabaseCRUD? = null

    private val backup: Database? = null
    private val backupReplicator: Replicator? = null

    override fun onCreate() {
        super.onCreate()

        startSession("test", "123456")
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
    private fun startSession(username: String, password: String?) {
        openDatabase(username)
        this.username = username
        startReplication(username, password)
        databaseCRUD = DatabaseCRUD(username, database!!)

        //localBackup(username)

        // TODO: After authenticated, move to next screen
        showMainUI()
    }

    // show loginUI
    private fun showMainUI() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // -------------------------
    // Database operation
    // -------------------------

    private fun openDatabase(dbname: String) {
        val config = DatabaseConfiguration(applicationContext)
        try {
            database = Database(dbname, config)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to create Database instance: %s - %s", e, dbname, config)
            // TODO: error handling
        }

    }

    private fun closeDatabase() {
        if (database != null) {
            try {
                database!!.close()
            } catch (e: CouchbaseLiteException) {
                Log.e(TAG, "Failed to close Database", e)
                // TODO: error handling
            }

        }
    }

    private fun createDatabaseIndex() {

    }

    // -------------------------
    // Replicator operation
    // -------------------------
    private fun startReplication(username: String?, password: String?) {
        if (!SYNC_ENABLED) return

        val uri: URI
        try {
            uri = URI(SYNCGATEWAY_URL)
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Failed parse URI: %s", e, SYNCGATEWAY_URL)
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

    private fun stopReplication() {
        if (!SYNC_ENABLED) return

        replicator!!.stop()
    }

    private fun runOnUiThread(runnable: Runnable) {
        Handler(applicationContext.mainLooper).post(runnable)
    }


    // --------------------------------------------------
    // ReplicatorChangeListener implementation
    // --------------------------------------------------
    override fun changed(change: ReplicatorChange) {
        Log.i(TAG, "[Todo] Replicator: status -> %s", change.status)
        if (change.status.error != null && change.status.error.code == 401) {
            Toast.makeText(applicationContext, "Authentication Error: Your username or password is not correct.", Toast.LENGTH_LONG).show()
            //logout()
        }
    }

    companion object {

        private val TAG = Application::class.java.simpleName

        private val SYNC_ENABLED = true

        private val DATABASE_NAME = "db"
        private val SYNCGATEWAY_URL = "ws://192.168.25.23:4984/db/"
    }
}