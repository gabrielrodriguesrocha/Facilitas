package com.ufscar.sor.dcomp.facilitas.activity

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.fragment.OrderFragment
import java.util.*
import android.view.LayoutInflater
import android.widget.*
import com.couchbase.lite.*
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import java.util.Calendar.*
import com.ufscar.sor.dcomp.facilitas.adapter.AutocompleteProductAdapter


class InputOrderActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private var fieldLinearLayout: LinearLayout? = null
    private var orderCRUD: DatabaseCRUD? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var productAutocompleteAdapter: AutocompleteProductAdapter? = null
    private var productIDs: MutableMap<String, Int> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_input)
        fieldLinearLayout = findViewById(R.id.field_layout)

        orderCRUD = (application as Application).getCrud()

        val calendarButton = this.findViewById(R.id.calendar) as Button

        val client = findViewById<TextView>(R.id.client)


        val product = findViewById<AutoCompleteTextView>(R.id.product)
        productAutocompleteAdapter = AutocompleteProductAdapter(this, orderCRUD!!.db, android.R.layout.select_dialog_item)
        product.threshold = 1//will start working from first character
        product.setAdapter(productAutocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            /*productIDs.put((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)*/
            product.tag = (adapterView.getItemAtPosition(i) as Result).getValue("id") as String
            Log.i(TAG, productIDs.toString())
            /*(adapterView.getItemAtPosition(i) as Result).getValue("id") as String*/
        }


        if (intent.getBooleanExtra(OrderFragment.INTENT_EDIT, false)) {
            val order = orderCRUD!!.readOrder(intent.getStringExtra(OrderFragment.INTENT_ORDER_ID))
            //input.maxLines = 1
            //input.setSingleLine(true)
            client.text = order!!.getString("client")

            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, order.getDate("deliveryDate").year,
                    order.getDate("deliveryDate").month, order.getDate("deliveryDate").day)

        }
        else {
            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, getInstance().get(YEAR),
                    getInstance().get(MONTH), getInstance().get(DAY_OF_MONTH))
        }

        calendarButton.setOnClickListener {
            datePickerDialog!!.show()
        }
    }

    fun onAddField(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.product_field, null)
        // Add the new row before the add field button.
        val product = rowView.findViewById<AutoCompleteTextView>(R.id.product)
        product.threshold = 1//will start working from first character
        product.setAdapter(productAutocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            /*productIDs.addString((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)*/
            product.tag = (adapterView.getItemAtPosition(i) as Result).getValue("id") as String
            Log.i(TAG, productIDs.toString())
        }
        fieldLinearLayout!!.addView(rowView, fieldLinearLayout!!.childCount)
    }

    fun onDeleteField(v: View) {
        fieldLinearLayout!!.removeView(v.parent as View)
    }

    fun onAddOrder() {
        val client = findViewById<EditText>(R.id.client)
        val products = (0..(fieldLinearLayout!!.childCount-1))
                                    .map { fieldLinearLayout!!.getChildAt(it) }
                                    /*.map { it.findViewById<AutoCompleteTextView>(R.id.product).tag; it.findViewById<AutoCompleteTextView>(R.id.amount) }*/
        products.forEach { it -> productIDs[it.findViewById<AutoCompleteTextView>(R.id.product).tag as String] = it.findViewById<TextView>(R.id.amount).text.toString().toInt() }
        /*val products = productIDs!!.map {orderCRUD!!.readProduct(it) }*/
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("BRT")
        calendar.set(datePickerDialog!!.datePicker.year, datePickerDialog!!.datePicker.month, datePickerDialog!!.datePicker.dayOfMonth)
        orderCRUD!!.createOrder(client.text.toString(), calendar.time, MutableDictionary(productIDs as Map<String, Int>))
        finish()
    }

    fun onCancelOrder(v: View) {
        finish()
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        Log.i(TAG, "Date changed!")
    }

    companion object {
        private val TAG = InputOrderActivity::class.java.simpleName
    }
}