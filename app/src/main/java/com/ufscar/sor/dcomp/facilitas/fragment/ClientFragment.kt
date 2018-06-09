package com.ufscar.sor.dcomp.facilitas.fragment

import android.annotation.SuppressLint
import android.widget.TextView
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import com.ufscar.sor.dcomp.facilitas.R


// In this case, the fragment displays simple text based on the page
class ClientFragment : Fragment() {

    private var mPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage = arguments!!.getInt(ARG_PAGE)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val fragmentView = inflater.inflate(R.layout.client_fragment, container, false)
        val textView = fragmentView.findViewById<TextView>(R.id.text)
        textView.text = getString(R.string.clients)
        return fragmentView
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"

        fun newInstance(page: Int): ClientFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = ClientFragment()
            fragment.arguments = args
            return fragment
        }
    }
}