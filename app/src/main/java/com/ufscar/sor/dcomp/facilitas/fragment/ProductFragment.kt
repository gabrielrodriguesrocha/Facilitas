package com.ufscar.sor.dcomp.facilitas.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import android.widget.*
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.activity.ProductDetailActivity
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.ProductAdapter
import java.util.*


// In this case, the fragment displays simple text based on the page
class ProductFragment : Fragment() {

    private var mPage: Int = 0

    private var username: String? = null
    private var db: Database? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage = arguments!!.getInt(ARG_PAGE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val mApplication = activity!!.application as Application
        username = mApplication.getUsername()
        db = mApplication.getDatabase()
        if (db == null) throw IllegalArgumentException()

        val fragmentView = inflater.inflate(R.layout.product_fragment, container, false)
        val listView = fragmentView.findViewById(R.id.product_list) as ListView?
        val adapter = ProductAdapter(activity!!, db)

        listView!!.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val id = adapter.getItem(i)
            val list = db!!.getDocument(id)
            showProductListView(list)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            val popup = PopupMenu(this@ProductFragment.context, view)
            popup.inflate(R.menu.product_item)
            popup.setOnMenuItemClickListener { item ->
                val id = adapter.getItem(pos)
                val list = db!!.getDocument(id)
                handleProductPopupAction(item, list)
            }
            popup.show()
            true
        }

        val button = fragmentView!!.findViewById(R.id.fab_product) as FloatingActionButton?
        button!!.setOnClickListener({ displayCreateDialog() })

        return fragmentView
    }

    private fun handleProductPopupAction(item: MenuItem, list: Document): Boolean {
        when (item.itemId) {
            R.id.update -> {
                displayUpdateDialog(list)
                return true
            }
            R.id.delete -> {
                deleteProduct(list)
                return true
            }
            else -> return false
        }
    }

    private fun showProductListView(list: Document) {
        val intent = Intent(this.context, ProductDetailActivity::class.java)
        intent.putExtra(OrderFragment.INTENT_ORDER_ID, list.id)
        startActivity(intent)
    }

    // display create list dialog
    private fun displayCreateDialog() {
        val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_new_product))
        val view = LayoutInflater.from(this@ProductFragment.context).inflate(R.layout.view_dialog_input, null)
        val input = view.findViewById(R.id.product) as EditText
        alert.setView(view)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            createProduct(title)
        })
        alert.setNegativeButton("Cancel") { _, _ -> }
        alert.show()
    }

    // display update list dialog
    private fun displayUpdateDialog(list: Document) {
        val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_update_product))
        val input = EditText(this.context)
        input.maxLines = 1
        input.setSingleLine(true)
        input.setText(list.getString("name"))
        alert.setView(input)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            updateProduct(list.toMutable(), title)
        })
        alert.show()
    }

    // -------------------------
    // Database - CRUD
    // -------------------------

    // create parcel
    private fun createProduct(title: String): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "product")
        mDoc.setString("name", title)
        mDoc.setString("owner", username)
        try {
            db!!.save(mDoc)
            return db!!.getDocument(mDoc.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, mDoc)
            //TODO: Error handling
            return null
        }

    }

    // update parcel
    private fun updateProduct(parcel: MutableDocument, title: String): Document? {
        parcel.setString("name", title)
        try {
            db!!.save(parcel)
            return db!!.getDocument(parcel.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, parcel)
            //TODO: Error handling
            return null
        }

    }

    // delete list
    private fun deleteProduct(parcel: Document): Document {
        try {
            db!!.delete(parcel)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to delete the doc - %s", e, parcel)
            //TODO: Error handling
        }

        return parcel
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        const val INTENT_LIST_ID = "list_id"
        private val TAG = ProductFragment::class.java.simpleName

        fun newInstance(page: Int): ProductFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = ProductFragment()
            fragment.arguments = args
            return fragment
        }
    }
}