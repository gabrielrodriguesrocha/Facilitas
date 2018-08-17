package com.ufscar.sor.dcomp.facilitas.activity

import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v4.view.ViewPager
import android.support.design.widget.TabLayout
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.CustomFragmentPagerAdapter
import android.support.v4.media.session.MediaButtonReceiver.handleIntent
import android.content.Intent
import android.app.SearchManager
import android.support.v4.view.MenuItemCompat.getActionView
import android.widget.SearchView
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import android.widget.ArrayAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import com.couchbase.lite.Authenticator
import com.couchbase.lite.internal.support.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.ufscar.sor.dcomp.facilitas.Application
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    var viewPager: ViewPager? = null
    var position: Int = 0
    private var PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private val httpClient = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager!!.adapter = CustomFragmentPagerAdapter(supportFragmentManager,
                this@MainActivity)

        // Give the TabLayout the ViewPager
        val tabLayout = findViewById<View>(R.id.sliding_tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        /*tabLayout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                position = tab.position
                Toast.makeText(this@MainActivity, position.toString(), Toast.LENGTH_SHORT).show()
            }
        })*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        inflater.inflate(R.menu.search, menu)

        /*val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Toast.makeText(this@MainActivity, query, Toast.LENGTH_SHORT).show()
                searchView.clearFocus()
                ((viewPager!!.adapter!! as CustomFragmentPagerAdapter).getItem(position) as OrderFragment).search(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })*/

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, 1)
                true
            }
            R.id.action_summary -> {
                val intent = Intent(this, SummaryActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSearchRequested(): Boolean {
        Toast.makeText(applicationContext, "Search!", Toast.LENGTH_LONG).show()
        return super.onSearchRequested()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            (viewPager!!.adapter!! as CustomFragmentPagerAdapter).refresh()
        }
    }
}
