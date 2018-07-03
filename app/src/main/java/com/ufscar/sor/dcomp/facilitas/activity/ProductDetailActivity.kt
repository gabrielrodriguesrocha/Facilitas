package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import com.ufscar.sor.dcomp.facilitas.fragment.ProductFragment
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import org.w3c.dom.Text
import java.lang.IllegalStateException

class ProductDetailActivity : AppCompatActivity() {
    private var productCRUD: DatabaseCRUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        productCRUD = (application as Application).getCrud()

        val product = productCRUD!!.readOrder(intent.getStringExtra(ProductFragment.INTENT_PRODUCT_ID)) ?: throw IllegalStateException()

        val name = findViewById<TextView>(R.id.name)
        val price = findViewById<TextView>(R.id.price)
        val category = findViewById<TextView>(R.id.category)

        name.text = product.getString("name")
        price.text = product.getDouble("price").toString()
        category.text = product.getString("category")



    }
}