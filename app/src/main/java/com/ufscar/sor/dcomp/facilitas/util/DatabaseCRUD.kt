package com.ufscar.sor.dcomp.facilitas.util

import android.app.Application
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
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
    fun createOrder(title: String): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "parcel")
        mDoc.setString("name", title)
        mDoc.setString("owner", username)
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
    private fun createProduct(title: String): Document? {
        val docId = username + "." + UUID.randomUUID()
        val mDoc = MutableDocument(docId)
        mDoc.setString("type", "product")
        mDoc.setString("name", title)
        mDoc.setString("owner", username)
        try {
            db.save(mDoc)
            return db.getDocument(mDoc.id)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "Failed to save the doc - %s", e, mDoc)
            //TODO: Error handling
            return null
        }

    }

    // update product
    private fun updateProduct(parcel: MutableDocument, title: String): Document? {
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
    private fun deleteProduct(parcel: Document): Document {
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