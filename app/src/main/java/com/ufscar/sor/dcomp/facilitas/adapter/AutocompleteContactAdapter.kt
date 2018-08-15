package com.ufscar.sor.dcomp.facilitas.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.couchbase.lite.*

import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.provider.ContactsContract
import android.content.ContentResolver
import com.ufscar.sor.dcomp.facilitas.util.ContactProjection


class AutocompleteContactAdapter(context: Context, val db: Database?, private val resource: Int) : ArrayAdapter<ContactProjection>(context, resource), Filterable {

    init {

    }

    private var suggestions: ArrayList<ContactProjection>? = null

    init {
        if (db == null) throw IllegalArgumentException()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val client = getItem(position)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(resource, parent, false)

        val text = mConvertView!!.findViewById(android.R.id.text1) as TextView
        //text.text = client.getString(ContactsContract.Contacts.DISPLAY_NAME)
        text.text = client.name

        //Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, client.getString(ContactsContract.Contacts.DISPLAY_NAME, product.getString("name"), product.getValue("name"), product.toMap())
        return mConvertView
    }

    // For custom autocomplete
    override fun getFilter(): Filter {
        return nameFilter
    }

    private var nameFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as ContactProjection).name
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            val cr = context.contentResolver
            if (constraint != null) {
                suggestions = arrayListOf()
                suggestions!!.clear()
                // TODO change to db side FTS
                val cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE ?",
                        arrayOf(constraint.toString().toLowerCase() + "%"), null)
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    val phone = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    if (suggestions!!.find { it -> (it.name == name) } == null)
                        suggestions!!.add(ContactProjection(name, id, phone))
                }
                filterResults.values = suggestions
                //filterResults.values = suggestions
                //filterResults.count = suggestions!!.size
                filterResults.count = cur.count
                cur.close()
                return filterResults
            } else {
                return FilterResults()
            }
            /*try {

                /*********** Reading Contacts Name And Number  */

                var phoneNumber = ""
                val cr = context.contentResolver

                //Query to get contact name

                val cur = cr
                        .query(ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null)

                // If data data found in contacts
                if (cur!!.count > 0) {

                    Log.i("AutocompleteContacts", "Reading   contacts........")

                    var k = 0
                    var name = ""

                    while (cur.moveToNext()) {

                        val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        //Check contact have phone number
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                            //Create query to get phone number by contact id
                            val pCur = cr
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                                            arrayOf<String>(id), null)
                            var j = 0

                            while (pCur!!.moveToNext()) {
                                // Sometimes get multiple data
                                if (j == 0) {
                                    // Get Phone number
                                    phoneNumber = "" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                    // Add contacts names to adapter
                                    add(name)

                                    // Add ArrayList names to adapter
                                    phoneValueArr.add(phoneNumber)
                                    nameValueArr.add(name)

                                    j++
                                    k++
                                }
                            }  // End while loop
                            pCur.close()
                        } // End if

                    }  // End while loop

                } // End Cursor value check
                cur.close()


            } catch (e: Exception) {
                Log.i("AutocompleteContacts", "Exception : $e")
            }*/

        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            /*val filteredList = results.values as ArrayList<Result>
            if (results.count > 0) {
                clear()
                results.values
                for (c in filteredList) {
                    add(c.getString("name"))
                }
                notifyDataSetChanged()
            }*/
            if (results?.count ?: -1 > 0) {
                clear()
                addAll(results!!.values as ArrayList<ContactProjection>)
                notifyDataSetChanged()
            }
            else {
                notifyDataSetInvalidated()
            }
        }
    }

    companion object {
        private val TAG = AutocompleteContactAdapter::class.java.simpleName
    }
}
