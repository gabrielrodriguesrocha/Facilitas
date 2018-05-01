package com.ufscar.sor.dcomp.facilitas

import android.app.SearchManager
import android.content.Intent
import android.widget.TextView
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.content.Intent.getIntent




// In this case, the fragment displays simple text based on the page
class PageFragment : Fragment() {

    private var mPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage = arguments!!.getInt(ARG_PAGE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        val textView = view as TextView
        textView.text = "$mPage"
        return view
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"

        fun newInstance(page: Int): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = PageFragment()
            fragment.arguments = args
            return fragment
        }
    }
}