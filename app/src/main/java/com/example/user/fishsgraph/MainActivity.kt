package com.example.user.fishsgraph

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val monthGraph : MonthGraph = findViewById(R.id.month_graph)
        monthGraph.setHorizontalPadding(this, R.dimen.abc_action_bar_content_inset_material);
        monthGraph.setStartDate(Date())
        monthGraph.setValues(listOf(10000, 50000, 10000, 500000))
        monthGraph.invalidate()
    }
}
