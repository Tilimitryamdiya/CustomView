package ru.netology.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.customview.ui.StatsView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statsView = findViewById<StatsView>(R.id.stats)

        statsView.totalData = 3500F
        statsView.currentDataList = listOf(
            450F,
            500F,
            350F,
            750F,
        )


    }
}