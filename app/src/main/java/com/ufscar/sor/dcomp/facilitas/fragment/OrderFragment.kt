package com.ufscar.sor.dcomp.facilitas.fragment

import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import android.widget.*

import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.internal.support.Log

import android.app.DatePickerDialog
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.activity.OrderDetailActivity
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.activity.InputOrderActivity
import com.ufscar.sor.dcomp.facilitas.adapter.OrderAdapter
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD

// This fragment displays all
class OrderFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        Log.i(TAG, "Date changed!")
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var username: String? = null
    private var db: Database? = null

    private var mPage: Int = 0

    private var orderCRUD: DatabaseCRUD? = null

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

        orderCRUD = DatabaseCRUD(username!!, db!!)

        val fragmentView = inflater.inflate(R.layout.order_fragment, container, false)
        val listView = fragmentView.findViewById(R.id.order_list) as ListView?
        val adapter = OrderAdapter(activity!!, db)
        listView!!.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val id = adapter.getItem(i)
            val list = db!!.getDocument(id)
            showOrderDetail(list)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            val popup = PopupMenu(this@OrderFragment.context, view)
            popup.inflate(R.menu.parcel_item)
            popup.setOnMenuItemClickListener { item ->
                val id = adapter.getItem(pos)
                val list = db!!.getDocument(id)
                handleParcelPopupAction(item, list)
            }
            popup.show()
            true
        }

        val button = fragmentView!!.findViewById(R.id.fab_order) as FloatingActionButton?
        button!!.setOnClickListener({ displayCreateDialog() })

        return fragmentView
    }

    private fun handleParcelPopupAction(item: MenuItem, order: Document): Boolean {
        when (item.itemId) {
            R.id.update -> {
                displayUpdateDialog(order)
                return true
            }
            R.id.delete -> {
                orderCRUD!!.deleteOrder(order)
                return true
            }
            else -> return false
        }
    }

    private fun showOrderDetail(order: Document) {
        val intent = Intent(this.context, OrderDetailActivity::class.java)
        intent.putExtra(INTENT_ORDER_ID, order.id)
        startActivity(intent)
    }


    // display create list dialog
    private fun displayCreateDialog() {
        val intent = Intent(this.context, InputOrderActivity::class.java)
        intent.putExtra(INTENT_EDIT, false)
        startActivity(intent)
    }

    // display update list dialog
    private fun displayUpdateDialog(order: Document) {
        /*
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
            orderCRUD!!.updateOrder(list.toMutable(), title)
        })
        alert.show()*/

        val intent = Intent(this.context, InputOrderActivity::class.java)
        intent.putExtra(INTENT_EDIT, true)
        intent.putExtra(INTENT_ORDER_ID, order.id)
        startActivity(intent)
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        const val INTENT_ORDER_ID = "order_id"
        const val INTENT_EDIT = "edit"
        private val TAG = OrderFragment::class.java.simpleName

        fun newInstance(page: Int): OrderFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = OrderFragment()
            fragment.arguments = args
            return fragment
        }
    }
}