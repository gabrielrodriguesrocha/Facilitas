package com.ufscar.sor.dcomp.facilitas.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.content.Context
import android.support.v4.app.FragmentPagerAdapter
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import com.ufscar.sor.dcomp.facilitas.fragment.ProductFragment


class CustomFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 2
    private val tabTitles = arrayOf("Encomendas", "Produtos")
    private val orderFragment = OrderFragment.newInstance(0)
    private val productFragment = ProductFragment.newInstance(1)

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> orderFragment
            1 -> productFragment
            else -> orderFragment
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }

    fun refresh() {
        orderFragment.refresh()
        productFragment.refresh()
    }
}