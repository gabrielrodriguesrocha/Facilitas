package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v4.view.ViewPager
import android.support.design.widget.TabLayout
import android.view.*
import android.widget.LinearLayout
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.CustomFragmentPagerAdapter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        val viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager.adapter = CustomFragmentPagerAdapter(supportFragmentManager,
                this@MainActivity)

        // Give the TabLayout the ViewPager
        val tabLayout = findViewById<View>(R.id.sliding_tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        inflater.inflate(R.menu.search, menu)

        return true
    }
}
