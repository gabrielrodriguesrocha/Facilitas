package com.ufscar.sor.dcomp.facilitas.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.couchbase.lite.*
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import java.lang.IllegalStateException


class SummaryActivity : AppCompatActivity() {
    private var db : Database? = null
    private var databaseCRUD : DatabaseCRUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        db = (application as Application).getDatabase() ?: throw IllegalStateException()
        databaseCRUD = (application as Application).getCrud() ?: throw IllegalStateException()

        val graph = findViewById<View>(R.id.graph) as GraphView


        /*val series = LineGraphSeries<DataPoint>(arrayOf(DataPoint(0.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0), DataPoint(3.0, 2.0), DataPoint(4.0, 6.0)))
        graph.addSeries(series)*/
        graph.addSeries(monthlySeries())
        graph.title = "Histórico Mensal"
    }

    private fun ordersPerMonth(): Query {
        return QueryBuilder.select(SelectResult.expression(Meta.id),
                                    SelectResult.property("products"),
                                    SelectResult.property("deliveryDate"))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order")))
                .groupBy(Expression.property("deliveryDate.month"))
                .orderBy(Ordering.property("deliveryDate").ascending())
    }

    private fun monthlySeries() : LineGraphSeries<DataPoint> {
        val rs = ordersPerMonth().execute()

        val series = rs.map {i ->
            var total: Double = 0.0

            Log.e(Application.TAG, "Current tuple: %s", i.toString())

            val productsDictionary = i.getDictionary("products")
            val products = productsDictionary.map { databaseCRUD!!.readProduct(it) }

            productsDictionary.forEachIndexed { idx, it ->
                val price = products[idx]!!.getDouble("price") * productsDictionary.getValue(it).toString().toDouble()

                total += price
            }
            DataPoint(i.getDate("deliveryDate"), total)
        }

        return LineGraphSeries(series.toTypedArray())
    }
}