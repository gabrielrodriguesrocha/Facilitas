package com.ufscar.sor.dcomp.facilitas.activity

import android.app.PendingIntent.getActivity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.couchbase.lite.*
import com.couchbase.lite.Function
import com.couchbase.lite.internal.support.Log
import com.ufscar.sor.dcomp.facilitas.R
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.ufscar.sor.dcomp.facilitas.Application
import com.ufscar.sor.dcomp.facilitas.util.DatabaseCRUD
import org.joda.time.LocalDate
import java.lang.IllegalStateException


@Suppress("DEPRECATION")
class SummaryActivity : AppCompatActivity() {
    private var db : Database? = null
    private var databaseCRUD : DatabaseCRUD? = null
    private var settings: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        settings = PreferenceManager.getDefaultSharedPreferences(this)

        db = (application as Application).getDatabase() ?: throw IllegalStateException()
        databaseCRUD = (application as Application).getCrud() ?: throw IllegalStateException()

        val graph = findViewById<View>(R.id.graph) as GraphView

        /*val series = LineGraphSeries<DataPoint>(arrayOf(DataPoint(0.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0), DataPoint(3.0, 2.0), DataPoint(4.0, 6.0)))
        graph.addSeries(series)*/
        graph.addSeries(monthlySeries())
        //graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        //graph.gridLabelRenderer.numHorizontalLabels = 3 // only 4 because of the space
        graph.title = "Histórico Mensal"

        val mSum = findViewById<TextView>(R.id.monthlySum)
        mSum.text = monthlySum().toString()

        val lmSum =findViewById<TextView>(R.id.lastMonthSum)
        lmSum.text = lastMonthSum().toString()

        val ySum = findViewById<TextView>(R.id.yearlySum)
        ySum.text = yearlySum().toString()

        val mClient = findViewById<TextView>(R.id.monthlyClient)
        mClient.text = monthlyClient() ?: ""

        val yClient = findViewById<TextView>(R.id.yearlyClient)
        yClient.text = yearlyClient() ?: ""
    }

    private fun ordersPerMonth(): Query {
        return QueryBuilder.select(SelectResult.expression(Meta.id),
                                    SelectResult.property("products"),
                                    SelectResult.property("deliveryDate"))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
                .groupBy(Expression.property("deliveryMonth"))
                .orderBy(Ordering.property("deliveryDate").ascending())
    }

    private fun monthlySum(): Double {
        val query = QueryBuilder.select(SelectResult.expression(Function.sum(Expression.property("orderPrice"))))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryMonth").equalTo(Expression.intValue(LocalDate().monthOfYear)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
        return query.execute().next().getDouble(0)
    }

    private fun lastMonthSum(): Double {
        val query = QueryBuilder.select(SelectResult.expression(Function.sum(Expression.property("orderPrice"))))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryMonth").equalTo(Expression.intValue(LocalDate().minusMonths(1).monthOfYear)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
        return query.execute().next().getDouble(0)
    }

    private fun yearlySum(): Double {
        val query = QueryBuilder.select(SelectResult.expression(Function.sum(Expression.property("orderPrice"))))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
        return query.execute().next().getDouble(0)
    }

    private fun monthlyClient(): String? {
        val query = QueryBuilder.select(SelectResult.property("client"),
                                        SelectResult.expression(Function.sum(Expression.property("orderPrice"))).`as`("price"))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryMonth").equalTo(Expression.intValue(LocalDate().monthOfYear)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
                .groupBy(Expression.property("client"))
                .orderBy(Ordering.property("price"))
                .limit(Expression.intValue(1))
        return query.execute().next()?.getString("client")
    }

    private fun yearlyClient(): String? {
        val query = QueryBuilder.select(SelectResult.property("client"),
                SelectResult.expression(Function.sum(Expression.property("orderPrice"))).`as`("price"))
                .from(DataSource.database(db))
                .where(Expression.property("type").equalTo(Expression.string("order"))
                        .and(Expression.property("group").equalTo(Expression.string(settings!!.getString("databaseGroup", "test"))))
                        .and(Expression.property("paid").equalTo(Expression.booleanValue(true)))
                        .and(Expression.property("deliveryYear").equalTo(Expression.intValue(LocalDate().year))))
                .groupBy(Expression.property("client"))
                .orderBy(Ordering.property("price"))
                .limit(Expression.intValue(1))
        return query.execute().next()?.getString("client")
    }

    private fun monthlySeries() : LineGraphSeries<DataPoint> {
        val rs = ordersPerMonth().execute()

        val series = rs.map {i ->
            var total = 0.0

            val productsDictionary = i.getDictionary("products")
            val products = productsDictionary.map { databaseCRUD!!.readProduct(it) }

            productsDictionary.forEachIndexed { idx, it ->
                val price = products[idx]!!.getDouble("price") * productsDictionary.getValue(it).toString().toDouble()

                total += price
            }
            DataPoint(i.getDate("deliveryDate").month.toDouble(), total)
        }

        return LineGraphSeries(series.toTypedArray())
    }
}