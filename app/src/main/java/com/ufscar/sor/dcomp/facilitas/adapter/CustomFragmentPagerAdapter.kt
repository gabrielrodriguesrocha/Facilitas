package com.ufscar.sor.dcomp.facilitas.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.content.Context
import android.support.v4.app.FragmentPagerAdapter
import com.ufscar.sor.dcomp.facilitas.fragment.ClientFragment
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import com.ufscar.sor.dcomp.facilitas.fragment.ProductFragment


class CustomFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 3
    private val tabTitles = arrayOf("Encomendas", "Produtos", "Clientes")

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment? {
        when(position) {
            0 -> return OrderFragment.newInstance(position)
            1 -> return ProductFragment.newInstance(position)
            2 -> return ClientFragment.newInstance(position)
            else -> return OrderFragment.newInstance(position)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }
}