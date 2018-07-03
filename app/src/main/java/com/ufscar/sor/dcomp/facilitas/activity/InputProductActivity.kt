package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.fragment.ProductFragment
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD

class InputProductActivity : AppCompatActivity() {
    private var productCRUD: DatabaseCRUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_input)
        productCRUD = (application as Application).getCrud()

        val name = findViewById<TextView>(R.id.name)
        val price = findViewById<TextView>(R.id.price)
        val category = findViewById<TextView>(R.id.category)

        val addButton = findViewById<Button>(R.id.add_product)
        val cancelButton = findViewById<Button>(R.id.cancel_product)

        if (intent.getBooleanExtra(ProductFragment.INTENT_EDIT, false)) {
            val product = productCRUD!!.readProduct(intent.getStringExtra(ProductFragment.INTENT_PRODUCT_ID))
                    ?: throw IllegalArgumentException()
            name.text = product.getString("name")
            price.text = product.getDouble("price").toString()
            category.text = product.getString("category")
            addButton.setOnClickListener{ v -> onUpdateProduct(v, intent.getStringExtra(ProductFragment.INTENT_PRODUCT_ID)) }
        }
        else {
            addButton.setOnClickListener{ v -> onAddProduct(v)}
        }
    }

    fun onAddProduct(v: View) {
        val name = findViewById<TextView>(R.id.name)
        val price = findViewById<TextView>(R.id.price)
        val category = findViewById<TextView>(R.id.category)
        productCRUD!!.createProduct(name.text.toString(), price.text.toString().toDouble(), category.text.toString())
        finish()
    }

    fun onUpdateProduct(v: View, id: String) {
        val name = findViewById<TextView>(R.id.name)
        val price = findViewById<TextView>(R.id.price)
        val category = findViewById<TextView>(R.id.category)
        productCRUD!!.updateProduct(id, name.text.toString(), price.text.toString().toDouble(), category.text.toString())
        finish()
    }

    fun onCancelProduct(v: View) {
        finish()
    }
}