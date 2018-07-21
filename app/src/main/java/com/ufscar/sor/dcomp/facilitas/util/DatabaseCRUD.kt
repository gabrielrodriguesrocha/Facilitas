package com.ufscar.sor.dcomp.facilitas.util

import com.couchbase.lite.*
import com.couchbase.lite.internal.support.Log
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
    fun updateProduct(id: String, name: String, price: Double, category: String?): Document? {
        val mDoc = MutableDocument(id)
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