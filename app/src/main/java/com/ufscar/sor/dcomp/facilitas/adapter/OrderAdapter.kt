package com.ufscar.sor.dcomp.facilitas.adapter

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.couchbase.lite.*

import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrderAdapter(context: Context, private val db: Database?) : ArrayAdapter<String>(context, 0), Filterable {
    private var ordersQuery: Query? = null
    private var incompTasksCountQuery: Query? = null
    private val incompCounts = HashMap<String, Int>()
    private val settings = PreferenceManager.getDefaultSharedPreferences(context)

    private var arrayList: ArrayList<String>? = ArrayList()
    private var mOriginalValues: ArrayList<String>? = null // Original Values

    init {

        if (db == null) throw IllegalArgumentException()

        this.ordersQuery = ordersQuery()
        this.ordersQuery!!.addChangeListener { change ->
            clear()
            val rs = change.results
            for (result in rs) {
                add(result.getString(0))
            }
            notifyDataSetChanged()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val id = getItem(position)
        val order = db!!.getDocument(id!!)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false)

        val text = mConvertView!!.findViewById(R.id.text) as TextView
        text.text = order.getString("client")

        val date = mConvertView.findViewById(R.id.date) as TextView
        // TODO change to get date
        val df = SimpleDateFormat("dd/MM", Locale.ENGLISH)
        date.text = df.format(order.getDate("deliveryDate"))


        val paid = mConvertView.findViewById(R.id.paid) as CheckBox
        paid.isChecked = order.getBoolean("paid")
        paid.setOnClickListener { _ ->
            val price = orderPrice(id)
            db.save(order.toMutable().setBoolean("paid", paid.isChecked).setDouble("orderPrice", price))
        }

        val delivered = mConvertView.findViewById(R.id.delivered) as CheckBox
        delivered.isChecked = order.getBoolean("delivered")
        delivered.setOnClickListener { _ ->

            db.save(order.toMutable().setBoolean("delivered", delivered.isChecked))
        }

        Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, order.id, order.getString("name"), order.getValue("name"), order.toMap())
        return mConvertView
    }

    private fun ordersQuery(): Query {
        val date = LocalDate().toDate()
        return QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings.getString("databaseGroup", "test"))))
                        .and(Expression.property("deliveryDate").greaterThanOrEqualTo(Expression.date(date))))
                .orderBy(Ordering.property("deliveryDate").ascending())
    }

    private fun ordersQuery(query: CharSequence): Query {
        //val ftsExpression = FullTextExpression.index("default")
        return QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings.getString("databaseGroup", "test"))))
                        .and(Expression.property("client").like(Expression.string(query.toString()))))
                .orderBy(Ordering.property("deliveryDate").ascending())
    }

    private fun orderPrice(id: String): Double {
        val order = db!!.getDocument(id)
        val products = order.getDictionary("products")
        val discount = order.getDouble("discount")
        var price = 0.0
        products.forEach { v-> price += db.getDocument(v).getDouble("price") * products.getDouble(v) }
        price -= discount
        return price
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun publishResults(constraint: CharSequence, results: FilterResults) {

                //arrayList = results.values as ArrayList<String>? // has the filtered values
                clear()
                /*for (i in arrayList!!)
                    add(i)*/
                val queryResults = ordersQuery(constraint).execute()
                for (rs in queryResults)
                    add(rs.getString(0))
                notifyDataSetChanged()  // notifies the data with new filtered values
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                /*
                var constraint = constraint
                var results = FilterResults()        // Holds the results of a filtering operation in values
                val filteredArrList = ArrayList<String>()

                if (mOriginalValues == null) {
                    mOriginalValues = ArrayList()
                    for (i in 0 until count) mOriginalValues!!.add(getItem(i))

                    //mOriginalValues = ArrayList<String>(arrayList) // saves the original data in mOriginalValues
                    //clear()
                }

                /********
                 *
                 * If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 * else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 */
                if (constraint == null || constraint.isEmpty()) {

                    // set the Original result to return
                    results.count = mOriginalValues!!.size
                    results.values = mOriginalValues
                }
                else {
                    constraint = constraint.toString().toLowerCase()
                    for (i in 0 until mOriginalValues!!.size) {
                        val data = mOriginalValues!![i]
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            filteredArrList.add(data)
                            //add(data)
                        }
                    }
                    // set the Filtered result to return
                    results.count = filteredArrList.size
                    results.values = filteredArrList
                }*/
                return FilterResults()
            }
        }
    }

    fun refresh() {
        clear()
        val queryResults = ordersQuery().execute()
        for (rs in queryResults)
            add(rs.getString(0))
        notifyDataSetChanged()  // notifies the data with new filtered values
    }

    companion object {
        private val TAG = OrderAdapter::class.java.simpleName
    }
}
