package edu.gatech.covidSpacer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val majorNum = findViewById<TextView>(R.id.majorNum)
        val minorNum = findViewById<TextView>(R.id.minorNum)

        val minor: Int = randomGenerate()
        val major: Int = randomGenerate()

        minorNum.text = minor.toString()
        majorNum.text = major.toString()
    }
    private fun randomGenerate(): Int {

        val bound = 65536
        //generate random values from 0-65536
        return Random(System.nanoTime()).nextInt(bound)
    }
}
