package com.ufscar.sor.dcomp.facilitas.adapter

import android.content.Context
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


class ProductAdapter(context: Context, private val db: Database?) : ArrayAdapter<String>(context, 0) {
    private var productsQuery: Query? = null
    private var suggestions: ArrayList<String>? = null

    init {

        if (db == null) throw IllegalArgumentException()

        this.productsQuery = productsQuery()
        this.productsQuery!!.addChangeListener { change ->
            clear()
            val rs = change.results
            for (result in rs) {
                add(result.getString(0))
            }
            notifyDataSetChanged()
        }

        this.suggestions = arrayListOf()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val id = getItem(position)
        val product = db!!.getDocument(id!!)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false)

        val text = mConvertView!!.findViewById(R.id.text) as TextView
        val price = mConvertView.findViewById(R.id.price) as TextView
        text.text = product.getString("name")
        price.text = context.resources.getString(R.string.priceAmount, product.getDouble("price"))

        Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, product.id, product.getString("name"), product.getValue("name"), product.toMap())
        return mConvertView
    }

    private fun productsQuery(): Query {
        return QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Expression.property("name")))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("product")))
                .orderBy(Ordering.property("name").ascending())
    }

    companion object {
        private val TAG = ProductAdapter::class.java.simpleName
    }
}
