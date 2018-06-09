package com.ufscar.sor.dcomp.facilitas.util

import android.app.Application
import com.couchbase.lite.*
import com.couchbase.lite.Array
import com.couchbase.lite.internal.support.Log
import com.couchbase.litecore.fleece.MArray
import java.util.*

// -------------------------
// Database - CRUD
// -------------------------

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
        set(db) {
            this._username = username
        }

    // -------------------------
    // Orders
    // -------------------------

    // create order
    fun createOrder(title: String, deliveryDate: Date, products: MutableDictionary): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "order")
        mDoc.setString("client", title)
        mDoc.setString("owner", username)
        mDoc.setDate("deliveryDate", deliveryDate)
        mDoc.setBoolean("delivered", false)
        mDoc.setBoolean("paid", false)
        // TODO change to join
        mDoc.setDictionary("products", products)
        try {
            db.save(mDoc)
            return db.getDocument(mDoc.id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, mDoc)
            //TODO: Error handling
            return null
        }

    }

    fun readOrder(id: String): Document? {
        try {
            return db.getDocument(id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to retrieve the doc - %s", e, id)
            //TODO: Error handling
            return null
        }
    }

    // update order
    fun updateOrder(order: MutableDocument, title: String): Document? {
        order.setString("name", title)
        try {
            db.save(order)
            return db.getDocument(order.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, order)
            //TODO: Error handling
            return null
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

    // create product
    fun createProduct(name: String, price: Double, category: String?): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "product")
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

    // read product
    fun readProduct(id: String): Document? {
        try {
            return db.getDocument(id)
        }
        catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to retrieve the doc - %s", e, id)
            //TODO: Error handling
            return null
        }
    }

    // update product
    fun updateProduct(parcel: MutableDocument, title: String): Document? {
        parcel.setString("name", title)
        try {
            db.save(parcel)
            return db.getDocument(parcel.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, parcel)
            //TODO: Error handling
            return null
        }

    }

    // delete product
    fun deleteProduct(parcel: Document): Document {
        try {
            db.delete(parcel)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to delete the doc - %s", e, parcel)
            //TODO: Error handling
        }

        return parcel
    }

    companion object {
        val db = null
        private val TAG = DatabaseCRUD::class.java.simpleName
    }
}