package com.example.des3776

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.des3776.databinding.ActivityMainBinding
import com.google.gson.JsonObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewmodel: HypertensionViewModel
    private var selectedIndex: Int = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewmodel = ViewModelProviders.of(this).get(HypertensionViewModel::class.java)
        binding.activity = this@MainActivity
        setRecyclerView()

        // link to Observer
        val dataObserver: Observer<ArrayList<Hypertension>> =
            Observer { observableData ->
                val newAdapter = HypertensionAdapter(this)
                newAdapter.data = observableData
                binding.genoRecyclerview.adapter = newAdapter
                Log.d("Observer", "looking ggod")
            }
        viewmodel.observableData.observe(this, dataObserver)

        val reqText: TextView = findViewById(R.id.desc_text)
        val radioGroup: RadioGroup = findViewById(R.id.radio_group)
        val radio1: RadioButton = findViewById(R.id.radio_1)
        val radio2: RadioButton = findViewById(R.id.radio_2)
        val radio3: RadioButton = findViewById(R.id.radio_3)
        val reqBtn: Button = findViewById(R.id.req_btn)

        // set attributes
        radioGroup.setOnCheckedChangeListener { radioGroup, index ->
            when (index) {
                R.id.radio_1 -> selectedIndex = 1
                R.id.radio_2 -> selectedIndex = 2
                R.id.radio_3 -> selectedIndex = 3
            }
            reqText.text = "You have selected " + selectedIndex.toString()
        }
        radio1.text = "First Test Case"
        radio2.text = "Second Test Case"
        radio3.text = "Third Test Case"
        reqBtn.text = "Send"
    }

    private fun setRecyclerView() {
        val hypertensionAdapter = HypertensionAdapter(this)
        binding.genoRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.genoRecyclerview.adapter = hypertensionAdapter
        hypertensionAdapter.notifyDataSetChanged()
    }

    fun getEvaluationOfSelectedTestCase(view: View) {

        val baseURL = "http://10.0.2.2:5000/"
        // use Retrofit to create request
        val retrofit =
            Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create())
                .build()
        val api = retrofit.create(HyperTensionAPI::class.java)
        val callGetEvaluation = api.getEvaluation(selectedIndex)

        callGetEvaluation.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val results = response.body()?.getAsJsonArray("result")

                if (results != null) {
                    val parsedHypertensions: MutableList<Hypertension> = mutableListOf()
                    for (res in results) {
                        val resObject = res.asJsonObject
                        val name = resObject.get("name").asString
                        val count = resObject.get("cnt").asInt
                        val maxP = resObject.get("max_p").asDouble
                        val minP = resObject.get("min_p").asDouble
                        // save to list
                        val item = Hypertension(name, count, maxP, minP)
                        parsedHypertensions.add(item)
                    }
                    Log.d("getEvaluationOfSelectedTestCase", parsedHypertensions.toString())
                    viewmodel.observableData.postValue(parsedHypertensions as ArrayList<Hypertension>?)
                }
                Log.d("onRequest", "success ${results.toString()}")
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d("onRequest", "failed $t")
            }
        })
    }
}
