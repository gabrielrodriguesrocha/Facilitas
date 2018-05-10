package com.ufscar.sor.dcomp.facilitas

import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.content.DialogInterface
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import android.widget.*

import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.internal.support.Log

import java.util.UUID


// In this case, the fragment displays simple text based on the page
class ParcelFragment : Fragment(){
    private var username: String? = null
    private var db: Database? = null

    private var mPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage = arguments!!.getInt(ARG_PAGE)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val mApplication = activity!!.application as com.ufscar.sor.dcomp.facilitas.Application
        username = mApplication.getUsername()
        db = mApplication.getDatabase()

        if (db == null) throw IllegalArgumentException()

        val fragmentView = inflater.inflate(R.layout.parcel_fragment, container, false)
        val listView = fragmentView.findViewById(R.id.parcel_list) as ListView?
        val adapter = ParcelAdapter(activity!!, db)
        listView!!.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val id = adapter.getItem(i)
            val list = db!!.getDocument(id)
            showParcelListView(list)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            val popup = PopupMenu(this@ParcelFragment.context, view)
            popup.inflate(R.menu.parcel_item)
            popup.setOnMenuItemClickListener { item ->
                val id = adapter.getItem(pos)
                val list = db!!.getDocument(id)
                handleParcelPopupAction(item, list)
            }
            popup.show()
            true
        }

        val button = fragmentView.findViewById(R.id.fab_parcel) as FloatingActionButton?
        button!!.setOnClickListener({ displayCreateDialog() })

        return fragmentView!!
    }

    private fun handleParcelPopupAction(item: MenuItem, list: Document): Boolean {
        when (item.itemId) {
            R.id.update -> {
                displayUpdateDialog(list)
                return true
            }
            R.id.delete -> {
                deleteParcel(list)
                return true
            }
            else -> return false
        }
    }

    private fun showParcelListView(list: Document) {
        val intent = Intent(this.context, ParcelDetailActivity::class.java)
        intent.putExtra(INTENT_LIST_ID, list.id)
        startActivity(intent)
    }

    // display create list dialog
    private fun displayCreateDialog() {
        val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_new_parcel))
        val view = LayoutInflater.from(this@ParcelFragment.context).inflate(R.layout.view_dialog_input, null)
        val input = view.findViewById(R.id.text) as EditText
        alert.setView(view)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            createParcel(title)
        })
        alert.setNegativeButton("Cancel") { _, _ -> }
        alert.show()
    }

    // display update list dialog
    private fun displayUpdateDialog(list: Document) {
        val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_update_parcel))
        val input = EditText(this.context)
        input.maxLines = 1
        input.setSingleLine(true)
        input.setText(list.getString("name"))
        alert.setView(input)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            updateParcel(list.toMutable(), title)
        })
        alert.show()
    }

    // -------------------------
    // Database - CRUD
    // -------------------------

    // create parcel
    private fun createParcel(title: String): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "parcel")
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
    private fun updateParcel(parcel: MutableDocument, title: String): Document? {
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
    private fun deleteParcel(parcel: Document): Document {
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
        private val TAG = ParcelFragment::class.java.simpleName

        fun newInstance(page: Int): ParcelFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = ParcelFragment()
            fragment.arguments = args
            return fragment
        }
    }
}