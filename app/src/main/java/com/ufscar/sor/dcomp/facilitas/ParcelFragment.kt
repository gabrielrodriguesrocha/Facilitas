package com.ufscar.sor.dcomp.facilitas

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupMenu
import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.EditText

import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.internal.support.Log

import java.util.UUID


// In this case, the fragment displays simple text based on the page
class ParcelFragment : Fragment(){

    private var listView: ListView? = null
    private var adapter: ParcelAdapter? = null

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

        listView = inflater.inflate(R.layout.parcel_fragment, container, false) as ListView?
        adapter = ParcelAdapter(activity!!, db)
        listView = activity!!.findViewById<View>(R.id.list) as ListView?
        // TODO Fix NullPointerException below
        listView!!.adapter = adapter
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val id = adapter!!.getItem(i)
            val list = db!!.getDocument(id)
            showParcelListView(list)
        }
        listView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            val popup = PopupMenu(this@ParcelFragment.context, view)
            popup.inflate(R.menu.parcel_item)
            popup.setOnMenuItemClickListener { item ->
                val id = adapter!!.getItem(pos)
                val list = db!!.getDocument(id)
                handleListPopupAction(item, list)
            }
            popup.show()
            true
        }

        return listView!!
    }

    private fun handleListPopupAction(item: MenuItem, list: Document): Boolean {
        when (item.itemId) {
            R.id.update -> {
                displayUpdateListDialog(list)
                return true
            }
            R.id.delete -> {
                deleteList(list)
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
    private fun displayCreateListDialog() {
        val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_new_parcel))
        val view = LayoutInflater.from(this@ParcelFragment.context).inflate(R.layout.view_dialog_input, null)
        val input = view.findViewById(R.id.text) as EditText
        alert.setView(view)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            createList(title)
        })
        alert.setNegativeButton("Cancel") { _, _ -> }
        alert.show()
    }

    // display update list dialog
    private fun displayUpdateListDialog(list: Document) {
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
            updateList(list.toMutable(), title)
        })
        alert.show()
    }

    // -------------------------
    // Database - CRUD
    // -------------------------

    // create list
    private fun createList(title: String): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "task-list")
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

    // update list
    private fun updateList(list: MutableDocument, title: String): Document? {
        list.setString("name", title)
        try {
            db!!.save(list)
            return db!!.getDocument(list.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, list)
            //TODO: Error handling
            return null
        }

    }

    // delete list
    private fun deleteList(list: Document): Document {
        try {
            db!!.delete(list)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to delete the doc - %s", e, list)
            //TODO: Error handling
        }

        return list
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