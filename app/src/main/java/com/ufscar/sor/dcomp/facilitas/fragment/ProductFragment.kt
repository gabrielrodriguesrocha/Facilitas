package com.ufscar.sor.dcomp.facilitas.fragment

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.app.Fragment
import android.view.View
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import android.widget.*
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.activity.ProductDetailActivity
import com.ufscar.sor.dcomp.facilitas.R
import com.ufscar.sor.dcomp.facilitas.activity.InputProductActivity
import com.ufscar.sor.dcomp.facilitas.adapter.ProductAdapter
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD


// In this case, the fragment displays simple text based on the page
class ProductFragment : Fragment() {

    private var mPage: Int = 0

    private var username: String? = null
    private var db: Database? = null

    private var productCRUD: DatabaseCRUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage = arguments!!.getInt(ARG_PAGE)
        productCRUD = (activity!!.application as Application).getCrud()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val fragmentView = inflater.inflate(R.layout.product_fragment, container, false)
        val listView = fragmentView.findViewById(R.id.product_list) as ListView?
        val adapter = ProductAdapter(activity!!, productCRUD!!.db)

        listView!!.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val id = adapter.getItem(i)
            val list = db!!.getDocument(id)
            showProductDetail(list)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, pos, _ ->
            val popup = PopupMenu(this@ProductFragment.context, view)
            popup.inflate(R.menu.product_item)
            popup.setOnMenuItemClickListener { item ->
                val id = adapter.getItem(pos)
                val product = productCRUD!!.readProduct(id) ?: throw IllegalArgumentException()
                handleProductPopupAction(item, product)
            }
            popup.show()
            true
        }

        val button = fragmentView!!.findViewById(R.id.fab_product) as FloatingActionButton?
        button!!.setOnClickListener({ displayCreateDialog() })

        return fragmentView
    }

    private fun handleProductPopupAction(item: MenuItem, product: Document): Boolean {
        when (item.itemId) {
            R.id.update -> {
                displayUpdateDialog(product)
                return true
            }
            R.id.delete -> {
                productCRUD!!.deleteOrder(product)
                return true
            }
            else -> return false
        }
    }

    private fun showProductDetail(product: Document) {
        val intent = Intent(this.context, ProductDetailActivity::class.java)
        intent.putExtra(OrderFragment.INTENT_ORDER_ID, product.id)
        startActivity(intent)
    }

    // display create list dialog
    private fun displayCreateDialog() {
        val intent = Intent(this.context, InputProductActivity::class.java)
        intent.putExtra(INTENT_EDIT, false)
        startActivity(intent)
    }

    // display update list dialog
    private fun displayUpdateDialog(product: Document) {
        /*val alert = AlertDialog.Builder(this.context)
        alert.setTitle(resources.getString(R.string.title_dialog_update_product))
        val input = EditText(this.context)
        input.maxLines = 1
        input.setSingleLine(true)
        input.setText(list.getString("name"))
        alert.setView(input)
        alert.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
            val title = input.text.toString()
            if (title.isEmpty())
                return@OnClickListener
            updateProduct(list.toMutable(), title)
        })
        alert.show()*/
        val intent = Intent(this.context, InputProductActivity::class.java)
        intent.putExtra(INTENT_EDIT, true)
        intent.putExtra(INTENT_PRODUCT_ID, product.id)
        startActivity(intent)
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        const val INTENT_PRODUCT_ID = "product_id"
        const val INTENT_EDIT = "edit"
        private val TAG = ProductFragment::class.java.simpleName

        fun newInstance(page: Int): ProductFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = ProductFragment()
            fragment.arguments = args
            return fragment
        }
    }
}