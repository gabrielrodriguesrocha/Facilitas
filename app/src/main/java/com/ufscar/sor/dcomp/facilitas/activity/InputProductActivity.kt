package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
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
        }
        addButton.setOnClickListener{ v -> onAddProduct(v) }
        cancelButton.setOnClickListener { _ -> finish()}
    }

    private fun onAddProduct(v: View) {
        //TODO refactor this
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val group = settings.getString("databaseGroup", "test")
        val name = findViewById<TextView>(R.id.name)
        if (name.text.toString() == "") {
            Toast.makeText(applicationContext, "Não há nome para o produto!", Toast.LENGTH_SHORT).show()
            return
        }
        val price = findViewById<TextView>(R.id.price)
        if (price.text.toString() == "") {
            Toast.makeText(applicationContext, "Não há preço!", Toast.LENGTH_SHORT).show()
            return
        }
        val category = findViewById<TextView>(R.id.category)
        when {
            intent.getBooleanExtra(OrderFragment.INTENT_EDIT, false) -> productCRUD!!.saveProduct(name.text.toString(), price.text.toString().toDouble(), group, category.text.toString(), id = intent.getStringExtra(ProductFragment.INTENT_PRODUCT_ID))
            else -> productCRUD!!.saveProduct(name.text.toString(), price.text.toString().toDouble(), group, category.text.toString())
        }
        finish()
    }
}