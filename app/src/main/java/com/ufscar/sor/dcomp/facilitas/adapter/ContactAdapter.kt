package com.ufscar.sor.dcomp.facilitas.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Function
import com.couchbase.lite.Meta
import com.couchbase.lite.Ordering
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.provider.ContactsContract



class ContactAdapter(context: Context) : ArrayAdapter<String>(context, 0) {
    var phones: Cursor? = null;

    init {
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        /*this.phones!!.addChangeListener { change ->
            clear()
            val rs = change.results
            for (result in rs) {
                add(result.getString(0))
            }
            notifyDataSetChanged()
        }*/
        /*while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

        }*/
        phones.close()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val client = getItem(position)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(R.layout.client_list_item, parent, false)

        val name = mConvertView!!.findViewById(R.id.name) as TextView
        phones.run {  }
        name.text = client.getString(phones.getString())

        val date = mConvertView!!.findViewById(R.id.date) as TextView
        // TODO change to get date
        val df = SimpleDateFormat("dd/MM", Locale.ENGLISH)
        date.text = df.format(order.getDate("deliveryDate"))


        val paid = mConvertView!!.findViewById(R.id.paid) as CheckBox
        paid.isChecked = order.getBoolean("paid")
        paid.setOnClickListener {
            _ -> db!!.save(order.toMutable().setBoolean("paid", paid.isChecked))
        }

        val delivered = mConvertView!!.findViewById(R.id.delivered) as CheckBox
        delivered.isChecked = order.getBoolean("delivered")
        delivered.setOnClickListener {
            _ -> db!!.save(order.toMutable().setBoolean("delivered", delivered.isChecked))
        }

        /*
        val countText = mConvertView.findViewById(R.id.task_count)
        if (incompCounts[order.id] != null) {
            countText.setText((incompCounts[order.id] as Int).toString())
        } else {
            countText.setText("")
        }*/

        Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, order.id, order.getString("name"), order.getValue("name"), order.toMap())
        return mConvertView
    }

    companion object {
        private val TAG = ContactAdapter::class.java.simpleName
    }
}
