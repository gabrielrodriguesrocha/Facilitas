package com.ufscar.sor.dcomp.facilitas.util

import android.preference.PreferenceManager
import com.couchbase.lite.*
import com.couchbase.lite.Dictionary
import com.couchbase.lite.internal.support.Log
import java.util.*

// -------------------------
// Database - CRUD
// -------------------------

@Suppress("DEPRECATION")
class DatabaseCRUD(private var _username: String, private var _db: Database) {

    // -------------------------
    // Getters and setters
    // -------------------------

    var db: Database
        get() {
            return _db
        }
        set(db) {
            this._db = db
        }

    var username: String
        get() {
            return _username
        }
        set(username) {
            this._username = username
        }

    // -------------------------
    // Orders
    // -------------------------

    // save order
    fun saveOrder(client: ContactProjection, deliveryDate: Date, deliveryTime: Pair<Int, Int>, products: MutableMap<String, Double>, group: String, discount: Double = 0.0, id: String = "update"): Document? {
        val docId = if (id != "update") id else username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "order")
        mDoc.setString("group", group)
        mDoc.setString("client", client.name)
        if (client.phone != "")
            mDoc.setString("phone", client.phone)
        mDoc.setDouble("discount", discount)
        mDoc.setString("owner", username)
        mDoc.setDate("deliveryDate", deliveryDate)
        mDoc.setInt("deliveryYear", deliveryDate.year)
        mDoc.setInt("deliveryMonth", deliveryDate.month+1)
        mDoc.setInt("deliveryHour", deliveryTime.first)
        mDoc.setInt("deliveryMinute", deliveryTime.second)
        mDoc.setBoolean("delivered", false)
        mDoc.setBoolean("paid", false)
        // TODO change to join
        val productsJoin = MutableDictionary()
        products.map { it -> productsJoin.setDouble(it.key, it.value) }
        // TODO FIX
        mDoc.setDictionary("products", productsJoin)
        return try {
            db.save(mDoc)
            db.getDocument(mDoc.id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, mDoc)
            //TODO: Error handling
            null
        }

    }

    fun readOrder(id: String): Document? {
        return try {
            db.getDocument(id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to retrieve the doc - %s", e, id)
            //TODO: Error handling
            null
        }
    }

    // delete list
    fun deleteOrder(order: Document): Document {
        try {
            db.delete(order)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to delete the doc - %s", e, order)
            //TODO: Error handling
        }

        return order
    }

    // -------------------------
    // Products
    // -------------------------

    // read product
    fun readProduct(id: String): Document? {
        return try {
            db.getDocument(id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to retrieve the doc - %s", e, id)
            //TODO: Error handling
            null
        }
    }

    // saveProduct
    fun saveProduct(name: String, price: Double, group: String, category: String?, id: String = "update"): Document? {
        val docId = if (id != "update") id else username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "product")
        mDoc.setString("group", group)
        mDoc.setString("name", name)
        mDoc.setString("owner", username)
        mDoc.setDouble("price", price)
        mDoc.setString("category", category)
        return try {
            db.save(mDoc)
            db.getDocument(mDoc.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, mDoc)
            //TODO: Error handling
            null
        }
    }

    // delete product
    fun deleteProduct(product: Document): Document {
        try {
            db.delete(product)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to delete the doc - %s", e, product)
            //TODO: Error handling
        }

        return product
    }

    companion object {
        val db = null
        private val TAG = DatabaseCRUD::class.java.simpleName
    }
}