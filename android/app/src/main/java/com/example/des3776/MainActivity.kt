package com.example.des3776

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var selectedIndex: Int = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val reqText: TextView = findViewById(R.id.desc_text)
        val radioGroup: RadioGroup = findViewById(R.id.radio_group)
        val reqBtn: Button = findViewById(R.id.req_btn)

        // set attributes
        reqText.text = "You have selected {} "
        radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{ radioGroup, index ->
            reqText.text = "You have selected " + (index + 1).toString() + " test case"
            selectedIndex = (index + 1)
        })
        reqBtn.setOnClickListener {  }

    }
    private fun getEvaluationOfSelectedTestCase() {

    }
}
