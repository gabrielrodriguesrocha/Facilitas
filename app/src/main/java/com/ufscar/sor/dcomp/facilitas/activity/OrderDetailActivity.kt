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
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDictionary
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
        val clientName = order.getString("client")
        val clientPhone = order.getString("phone")
        client.text = "$clientName ($clientPhone)"

        val date = findViewById<TextView>(R.id.date)
        val df = SimpleDateFormat("dd/MM", Locale.ENGLISH)
        val deliveryDate = df.format(order.getDate("deliveryDate"))
        val deliveryHour = order.getInt("deliveryHour")
        val deliveryMinute = order.getInt("deliveryMinute")
        date.text = "$deliveryDate ($deliveryHour h $deliveryMinute m)"

        val linearLayout = findViewById<LinearLayout>(R.id.order_products)

        var total: Double = 0.0

        //val productsDictionary = order.getDictionary("products")
        //val products = productsDictionary.map { orderCRUD!!.readProduct(it) }
        val products = order.getValue("products") as Dictionary

        products.forEach { key ->
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rowView = inflater.inflate(R.layout.view_product_item, null)
            val product = orderCRUD!!.readProduct(key)

            rowView.findViewById<TextView>(R.id.amount).text = products.getDouble(key).toString()
            rowView.findViewById<TextView>(R.id.name).text = product!!.getString("name")
            val price = product.getDouble("price") * products.getDouble(key)
            rowView.findViewById<TextView>(R.id.price).text = resources.getString(R.string.priceAmount, price)

            total += price

            linearLayout!!.addView(rowView, linearLayout.childCount)
        }

        val discountOrder = findViewById<TextView>(R.id.discount)
        discountOrder.text = resources.getString(R.string.priceAmount, order.getDouble("discount"))

        total -= order.getDouble("discount")

        val totalOrder = findViewById<TextView>(R.id.total)
        totalOrder.text = resources.getString(R.string.priceAmount, total)
    }
}