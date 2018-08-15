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
import android.provider.ContactsContract
import android.content.Intent
import android.R.attr.button
import android.app.Activity
import android.app.TimePickerDialog
import android.os.AsyncTask.execute
import android.preference.PreferenceManager
import android.text.TextWatcher
import com.ufscar.sor.dcomp.facilitas.adapter.AutocompleteContactAdapter
import com.ufscar.sor.dcomp.facilitas.util.ContactProjection
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.order_input.*
import android.widget.TimePicker
import org.joda.time.LocalDate


@Suppress("DEPRECATION")
class InputOrderActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private var fieldLinearLayout: LinearLayout? = null
    private var orderCRUD: DatabaseCRUD? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null
    private var deliveryTime: Pair<Int, Int>? = null
    private var productAutocompleteAdapter: AutocompleteProductAdapter? = null
    private var contactAutocompleteAdapter: AutocompleteContactAdapter? = null
    private var productIDs: MutableMap<String, Double> = mutableMapOf()
    private var client: ContactProjection? = null
    private var totalView: TextView? = null
    private var total = 0.0
    private var PICK_CONTACT = 2015

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_input)
        fieldLinearLayout = findViewById(R.id.field_layout)

        orderCRUD = (application as Application).getCrud()

        val calendarButton = this.findViewById(R.id.calendar) as Button
        val timeButton = this.findViewById(R.id.time) as Button

        totalView = findViewById(R.id.total)

        val discount = findViewById<EditText>(R.id.discount)

        val clientView = findViewById<AutoCompleteTextView>(R.id.client)
        contactAutocompleteAdapter = AutocompleteContactAdapter(this, orderCRUD!!.db, android.R.layout.select_dialog_item)
        clientView.threshold = 1
        clientView.setAdapter(contactAutocompleteAdapter)
        clientView.setOnItemClickListener { adapterView, _, i, _ ->
            clientView.tag = (adapterView.getItemAtPosition(i) as ContactProjection).phone
            client = ContactProjection((adapterView.getItemAtPosition(i) as ContactProjection).name,
                                       (adapterView.getItemAtPosition(i) as ContactProjection).id,
                                       (adapterView.getItemAtPosition(i) as ContactProjection).phone)
        }

        productAutocompleteAdapter = AutocompleteProductAdapter(this, orderCRUD!!.db, android.R.layout.select_dialog_item)
        /*
        val rowView = findViewById<LinearLayout>(R.id.product_field)
        val product = findViewById<AutoCompleteTextView>(R.id.product)
        val amount = findViewById<EditText>(R.id.amount)
        val price = rowView.findViewById<TextView>(R.id.price)
        productAutocompleteAdapter = AutocompleteProductAdapter(this, orderCRUD!!.db, android.R.layout.select_dialog_item)
        product.threshold = 1//will start working from first character
        product.setAdapter(productAutocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            /*productIDs.put((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)*/
            product.tag = (adapterView.getItemAtPosition(i) as Result).getValue("id") as String
            amount.setText("1.0")
            rowView.tag = (adapterView.getItemAtPosition(i) as Result).getValue("price").toString()
            price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
            Log.i(TAG, productIDs.toString())
            /*(adapterView.getItemAtPosition(i) as Result).getValue("id") as String*/
        }
        amount.setOnFocusChangeListener { _, _ ->
            if (rowView.tag != null) price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
        }

        */


        if (intent.getBooleanExtra(OrderFragment.INTENT_EDIT, false)) {
            val order = orderCRUD!!.readOrder(intent.getStringExtra(OrderFragment.INTENT_ORDER_ID))
            //input.maxLines = 1
            //input.setSingleLine(true)
            clientView.setText(order!!.getString("client"))

            val products = order.getDictionary("products")
            products.forEachIndexed { _, key ->
                val productIdx = orderCRUD!!.readProduct(key)
                /*
                if (i == 0) {
                    product.tag = key
                    product.setText(productIdx!!.getString("name"))
                    amount.setText(products.getDouble(key).toString())
                }
                else {*/
                    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rowView = inflater.inflate(R.layout.product_field, null)
                    // Add the new row before the add field button.
                    val product = rowView.findViewById<AutoCompleteTextView>(R.id.product)
                    val amount = rowView.findViewById<EditText>(R.id.amount)
                    val price = rowView.findViewById<TextView>(R.id.price)
                    rowView.tag = productIdx!!.getDouble("price")
                    product.threshold = 1//will start working from first character
                    product.setAdapter(productAutocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
                    product.setOnItemClickListener { adapterView, _, i, _ ->
                        /*productIDs.addString((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)*/
                        product.tag = (adapterView.getItemAtPosition(i) as Result).getValue("id") as String
                        amount.setText("1.0")
                        rowView.tag = (adapterView.getItemAtPosition(i) as Result).getValue("price").toString()
                        price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
                        total += rowView.tag.toString().toDouble() * amount.text.toString().toDouble()
                        totalView!!.text = total.toString()
                        Log.i(TAG, productIDs.toString())
                    }
                    amount.setOnFocusChangeListener { _, _ ->
                        if (rowView.tag != null) price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
                    }
                    total += productIdx.getDouble("price") * products.getDouble(key)
                    product.tag = key
                    amount.setText(products.getDouble(key).toString())
                    product.setText(productIdx.getString("name"))
                    price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
                    fieldLinearLayout!!.addView(rowView, fieldLinearLayout!!.childCount)
                /*}*/
            }

            totalView!!.text = total.toString()

            discount.setText(order.getDouble("discount").toString())

            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, order.getInt("deliveryYear"),
                    order.getInt("deliveryMonth")-1, order.getInt("deliveryDay"))

            timePickerDialog = TimePickerDialog(
                    this, mTimeSetListener, order.getInt("deliveryHour"),
                    order.getInt("deliveryMinute"), true)
            deliveryTime = Pair(order.getInt("deliveryHour"), order.getInt("deliveryMinute"))

        }
        else {
            onAddField(fieldLinearLayout!!)
            datePickerDialog = DatePickerDialog(
                    this, this@InputOrderActivity, getInstance().get(YEAR),
                    getInstance().get(MONTH), getInstance().get(DAY_OF_MONTH))
            timePickerDialog = TimePickerDialog(
                    this, mTimeSetListener, getInstance().get(HOUR),
                    getInstance().get(MINUTE), true)
            deliveryTime = Pair(getInstance().get(HOUR), getInstance().get(MINUTE))
        }

        calendarButton.setOnClickListener {
            datePickerDialog!!.show()
        }

        timeButton.setOnClickListener {
            timePickerDialog!!.show()
        }

        discount.setOnFocusChangeListener { _, _ ->
                if (discount.text.toString() != "") {
                    total -= discount.text.toString().toDouble()
                    totalView!!.text = total.toString()
                }
        }

        /*findViewById<Button>(R.id.client).setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(i, PICK_CONTACT)
        }*/
    }

    fun onAddField(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.product_field, null)
        // Add the new row before the add field button.
        val product = rowView.findViewById<AutoCompleteTextView>(R.id.product)
        val amount = rowView.findViewById<EditText>(R.id.amount)
        val price = rowView.findViewById<TextView>(R.id.price)
        product.threshold = 1//will start working from first character
        product.setAdapter(productAutocompleteAdapter)//setting the adapter data into the AutoCompleteTextView
        product.setOnItemClickListener { adapterView, _, i, _ ->
            /*productIDs.addString((adapterView.getItemAtPosition(i) as Result).getValue("id") as String)*/
            product.tag = (adapterView.getItemAtPosition(i) as Result).getValue("id") as String
            rowView.tag = (adapterView.getItemAtPosition(i) as Result).getValue("price").toString()
            amount.setText("1.0")
            rowView.tag = (adapterView.getItemAtPosition(i) as Result).getValue("price").toString()
            price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
            total += rowView.tag.toString().toDouble() * amount.text.toString().toDouble()
            totalView!!.text = total.toString()
            Log.i(TAG, productIDs.toString())
        }
        amount.setOnFocusChangeListener { _, _ ->
            if (rowView.tag != null) {
                total -= price.text.toString().toDouble()
                price.text = (rowView.tag.toString().toDouble() * amount.text.toString().toDouble()).toString()
                total += price.text.toString().toDouble()
                totalView!!.text = total.toString()
            }
        }
        fieldLinearLayout!!.addView(rowView, fieldLinearLayout!!.childCount)
    }

    fun onDeleteField(v: View) {
        if (((v.parent as View).parent as LinearLayout).childCount == 1) {
            //Toast.makeText(applicationContext, "Não há produtos!", Toast.LENGTH_SHORT).show()
            return
        }
        val price = (v.parent as View).findViewById<TextView>(R.id.price).text.toString()
        if (price != "") {
            total -= (v.parent as View).findViewById<TextView>(R.id.price).text.toString().toDouble()
            totalView!!.text = total.toString()
        }
        fieldLinearLayout!!.removeView(v.parent as View)
    }

    private val mTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        deliveryTime = Pair(hourOfDay, minute)
    }

    fun onAddOrder(v: View) {
        val clientView = findViewById<AutoCompleteTextView>(R.id.client)
        //TODO refactor this
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val group = settings.getString("databaseGroup", "test")
        val products = (0..(fieldLinearLayout!!.childCount-1))
                                    .map { fieldLinearLayout!!.getChildAt(it) }
                                    /*.map { it.findViewById<AutoCompleteTextView>(R.id.product).tag; it.findViewById<AutoCompleteTextView>(R.id.amount) }*/
        products.forEach { it ->
            if (it.findViewById<AutoCompleteTextView>(R.id.product).tag != null) {
                if (productIDs.contains(it.findViewById<AutoCompleteTextView>(R.id.product).tag as String))
                    productIDs[it.findViewById<AutoCompleteTextView>(R.id.product).tag as String] = productIDs[it.findViewById<AutoCompleteTextView>(R.id.product).tag as String]!!.plus(it.findViewById<TextView>(R.id.amount).text.toString().toDouble())
                else
                    productIDs[it.findViewById<AutoCompleteTextView>(R.id.product).tag as String] = it.findViewById<TextView>(R.id.amount).text.toString().toDouble()
            }
        }
        if (productIDs.isEmpty()) {
            Toast.makeText(applicationContext, "Não há produtos!", Toast.LENGTH_SHORT).show()
            return
        }

        val discount = findViewById<TextView>(R.id.discount)
        /*val products = productIDs!!.map {orderCRUD!!.readProduct(it) }*/

        /*val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("BRT")
        calendar.set(datePickerDialog!!.datePicker.year, datePickerDialog!!.datePicker.month, datePickerDialog!!.datePicker.dayOfMonth)*/
        val date = LocalDate(datePickerDialog!!.datePicker.year, datePickerDialog!!.datePicker.month + 1, datePickerDialog!!.datePicker.dayOfMonth)

        if (clientView.text.toString() == "") {
            Toast.makeText(applicationContext, "Não há cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        if (client == null) client = ContactProjection(clientView.text.toString())

        if (discount.text.toString() == "") discount.text = "0.0"
        when {
            intent.getBooleanExtra(OrderFragment.INTENT_EDIT, false) -> orderCRUD!!.saveOrder(client!!, date, deliveryTime!!, productIDs, group, discount = discount.text.toString().toDouble(), id = intent.getStringExtra(OrderFragment.INTENT_ORDER_ID))
            else -> orderCRUD!!.saveOrder(client!!, date, deliveryTime!!, productIDs, group, discount.text.toString().toDouble())
        }
        finish()
    }

    fun onCancelOrder(v: View) {
        finish()
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        Log.i(TAG, "Date changed!")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            val contactUri = data!!.data
            val cursor = contentResolver.query(contactUri!!, null, null, null, null)
            cursor!!.moveToFirst()
            val phone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val name = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val client = findViewById<TextView>(R.id.client)
            client.text = cursor.getString(name)
            client.tag = cursor.getString(phone)

            for (i in cursor.columnNames) {
                Log.i(TAG, i)
            }
            cursor.close()
        }
    }

    companion object {
        private val TAG = InputOrderActivity::class.java.simpleName
    }
}