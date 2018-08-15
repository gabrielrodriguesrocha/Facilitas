package com.ufscar.sor.dcomp.facilitas.adapter

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.couchbase.lite.*
import com.couchbase.lite.Function
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R


class AutocompleteProductAdapter(context: Context, private val db: Database?, private val resource: Int) : ArrayAdapter<Result>(context, resource), Filterable {
    private var productsQuery: Query? = null
    private var mProducts: List<Result>? = null
    private var suggestions: ArrayList<Result>? = null
    private val settings = PreferenceManager.getDefaultSharedPreferences(context)


    init {

        if (db == null) throw IllegalArgumentException()

        this.productsQuery = productsQuery()
        /*this.productsQuery!!.addChangeListener { change ->
            clear()
            val rs = change.results
            for (result in rs) {
                add(result.getString(0))
            }
            notifyDataSetChanged()
        }*/
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val product = getItem(position)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(resource, parent, false)

        val text = mConvertView!!.findViewById(android.R.id.text1) as TextView
        text.text = product.getString("name")

        Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, product.getString("id"), product.getString("name"), product.getValue("name"), product.toMap())
        return mConvertView
    }

    // For custom autocomplete
    override fun getFilter(): Filter {
        return nameFilter
    }

    private var nameFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as Result).getString("name")
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null) {
                suggestions = arrayListOf()
                suggestions!!.clear()
                // TODO change to db side FTS
                productsQuery!!.execute().allResults()
                        .filter { it.getString("name").toLowerCase().startsWith(constraint.toString().toLowerCase()) }
                        .forEach { suggestions!!.add(it) }
                filterResults.values = suggestions
                filterResults.count = suggestions!!.size
                return filterResults
            } else {
                return FilterResults()
            }
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
                addAll(results!!.values as ArrayList<Result>)
                notifyDataSetChanged()
            }
            else {
                notifyDataSetInvalidated()
            }
        }
    }

    private fun productsQuery(): Query {
        return QueryBuilder.select(SelectResult.expression(Meta.id),
                                   SelectResult.expression(Expression.property("name")),
                                   SelectResult.expression(Expression.property("price")))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("product"))
                        .and(Expression.property("group").equalTo(Expression.string(settings.getString("databaseGroup", "test")))))
                .orderBy(Ordering.property("name").ascending())
    }

    companion object {
        private val TAG = AutocompleteProductAdapter::class.java.simpleName
    }
}
