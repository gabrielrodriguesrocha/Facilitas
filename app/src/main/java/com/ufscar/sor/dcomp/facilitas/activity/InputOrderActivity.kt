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
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import java.util.Calendar.*
import com.couchbase.lite.Document
import com.couchbase.lite.Result
import com.ufscar.sor.dcomp.facilitas.adapter.AutocompleteProductAdapter


class InputOrderActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private var fieldLinearLayout: LinearLayout? = null
    private var orderCRUD: DatabaseCRUD? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var autocompleteAdapter: AutocompleteProductAdapter? = null
    private var productIDs: ArrayList<String>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_input)
        fieldLinearLayout = findViewById(R.id.field_layout)
        orderCRUD = DatabaseCRUD((application as Application).getUsername()!!, (application as Application).getDatabase()!!)

        val input = findViewById<TextView>(R.id.client)
        val calendarButton = this.findViewById(R.id.calendar) as Button

        val product = findViewById<AutoCompleteTextView>(R.id.product)
        autocompleteAdapter = AutocompleteProductAdapter(this, orderCRUD!!.db, android.R.layout.select_dialog_item)
        product.threshold = 1//will start working from first character
        product.setAdapter(autocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            productIDs!!.add((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)
            Log.i(TAG, productIDs.toString())
        }


        if (intent.getBooleanExtra(OrderFragment.INTENT_EDIT, false)) {
            val order = orderCRUD!!.readOrder(intent.getStringExtra(OrderFragment.INTENT_ORDER_ID))
            //input.maxLines = 1
            //input.setSingleLine(true)
            input.text = order!!.getString("name")
            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, getInstance().get(YEAR),
                    getInstance().get(MONTH), getInstance().get(DAY_OF_MONTH))
            calendarButton.setOnClickListener {
                datePickerDialog!!.show()
            }
        }
        else {
            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, getInstance().get(YEAR),
                    getInstance().get(MONTH), getInstance().get(DAY_OF_MONTH))

            calendarButton.setOnClickListener {
                datePickerDialog!!.show()
            }
        }
    }

    fun onAddField(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.product_field, null)
        // Add the new row before the add field button.
        val product = rowView.findViewById<AutoCompleteTextView>(R.id.product)
        product.threshold = 1//will start working from first character
        product.setAdapter(autocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            productIDs!!.add((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)
            Log.i(TAG, productIDs.toString())
        }
        fieldLinearLayout!!.addView(rowView, fieldLinearLayout!!.childCount)
    }

    fun onDeleteField(v: View) {
        fieldLinearLayout!!.removeView(v.parent as View)
    }

    fun onAddOrder(v: View) {
        val client = findViewById<EditText>(R.id.client)
        /*
        val products = (0..fieldLinearLayout!!.childCount)
                                    .map { fieldLinearLayout!!.getChildAt(it) }
                                    .map { it.findViewById<AutoCompleteTextView>(R.id.product) }
                                    .map { it.text }
        */
        // TODO handle dates better
        val date = intArrayOf(datePickerDialog!!.datePicker.year, datePickerDialog!!.datePicker.month, datePickerDialog!!.datePicker.dayOfMonth)
        orderCRUD!!.createOrder(client.text.toString())
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