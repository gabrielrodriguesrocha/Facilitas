package com.ufscar.sor.dcomp.facilitas.activity

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.couchbase.lite.DataSource
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.adapter.CustomFragmentPagerAdapter
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import org.w3c.dom.Text
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {
    private var orderCRUD: DatabaseCRUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        orderCRUD = (application as Application).getCrud() ?: throw IllegalStateException()

        val order = orderCRUD!!.readOrder(intent.getStringExtra(OrderFragment.INTENT_ORDER_ID)) ?: throw IllegalStateException()

        val client = findViewById<TextView>(R.id.client)
        client.text = order.getString("client")

        val date = findViewById<TextView>(R.id.date)
        val df = SimpleDateFormat("dd/MM", Locale.ENGLISH)
        date.text = df.format(order.getDate("deliveryDate"))

        val linearLayout = findViewById<LinearLayout>(R.id.order_products)

        var total: Double = 0.0

        val productsDictionary = order.getDictionary("products")
        val products = productsDictionary.map { orderCRUD!!.readProduct(it) }

        productsDictionary.forEachIndexed { idx, it ->
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rowView = inflater.inflate(R.layout.view_product_item, null)

            rowView.findViewById<TextView>(R.id.amount).text = productsDictionary.getValue(it).toString()
            rowView.findViewById<TextView>(R.id.name).text = products[idx]!!.getString("name")
            val price = products[idx]!!.getDouble("price") * productsDictionary.getValue(it).toString().toDouble()
            rowView.findViewById<TextView>(R.id.price).text = resources.getString(R.string.priceAmount, price)

            total += price

            linearLayout!!.addView(rowView, linearLayout.childCount)
        }

        val totalOrder = findViewById<TextView>(R.id.total)
        totalOrder.text = resources.getString(R.string.priceAmount, total)
    }
}