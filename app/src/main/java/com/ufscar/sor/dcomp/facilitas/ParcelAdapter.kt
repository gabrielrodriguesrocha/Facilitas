package com.ufscar.sor.dcomp.facilitas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Function
import com.couchbase.lite.Meta
import com.couchbase.lite.Ordering
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import com.couchbase.lite.internal.support.Log

import java.util.HashMap

class ParcelAdapter(context: Context, private val db: Database?) : ArrayAdapter<String>(context, 0) {
    private var parcelsQuery: Query? = null
    private var incompTasksCountQuery: Query? = null
    private val incompCounts = HashMap<String, Int>()

    init {

        if (db == null) throw IllegalArgumentException()

        this.parcelsQuery = listsQuery()
        this.parcelsQuery!!.addChangeListener { change ->
            clear()
            val rs = change.results
            for (result in rs) {
                add(result.getString(0))
            }
            notifyDataSetChanged()
        }

        this.incompTasksCountQuery = incompTasksCountQuery()
        this.incompTasksCountQuery!!.addChangeListener { change ->
            incompCounts.clear()
            val rs = change.results
            for (result in rs) {
                Log.e(TAG, "result -> " + result.toMap())
                incompCounts[result.getString(0)] = result.getInt(1)
            }
            notifyDataSetChanged()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val id = getItem(position)
        val list = db!!.getDocument(id!!)
        if (mConvertView == null)
            mConvertView = LayoutInflater.from(context).inflate(R.layout.parcel_view, parent, false)

        val text = mConvertView!!.findViewById(R.id.text) as TextView
        text.text = list.getString("name")

        /*
        val countText = mConvertView.findViewById(R.id.task_count)
        if (incompCounts[list.id] != null) {
            countText.setText((incompCounts[list.id] as Int).toString())
        } else {
            countText.setText("")
        }*/

        Log.e(TAG, "getView(): pos -> %d, docID -> %s, name -> %s, name2 -> %s, all -> %s", position, list.id, list.getString("name"), list.getValue("name"), list.toMap())
        return mConvertView
    }

    private fun listsQuery(): Query {
        return QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("parcel")))
                .orderBy(Ordering.property("name").ascending())
    }

    private fun incompTasksCountQuery(): Query {
        val exprType = Expression.property("type")
        val exprComplete = Expression.property("complete")
        val exprTaskListId = Expression.property("taskList.id")
        val srTaskListID = SelectResult.expression(exprTaskListId)
        val srCount = SelectResult.expression(Function.count(Expression.all()))
        return QueryBuilder.select(srTaskListID, srCount)
                .from(DataSource.database(db))
                .where(exprType.equalTo(Expression.string("parcel")).and(exprComplete.equalTo(Expression.booleanValue(false))))
                .groupBy(exprTaskListId)
    }

    companion object {
        private val TAG = ParcelAdapter::class.java.simpleName
    }
}
