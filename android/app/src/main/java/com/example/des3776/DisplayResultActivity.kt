package com.example.des3776

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.des3776.databinding.ActivityDisplayResultBinding

/**
 *  TODO
 *  Seperate these Activities into Fragments
 *  so that we can share ViewModel's liveData
 *  need to use 1 Activity not 2
 */

class DisplayResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisplayResultBinding
    private lateinit var viewmodel: HypertensionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get data from intent
        val receivedData =
            this.intent.getSerializableExtra("receivedData") as ArrayList<Hypertension>
        val userName = this.intent.getStringExtra("userName")
        val userAge = this.intent.getStringExtra("userAge")
        val userDisease = this.intent.getStringExtra("userDisease")
        val userMedication = this.intent.getStringExtra("userMedication")

        Log.d("receivedData", receivedData.toString())
        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_result)
        viewmodel = ViewModelProvider(this)[HypertensionViewModel::class.java]

        binding.activity = this@DisplayResultActivity

        // link to Observer
        val dataObserver: Observer<ArrayList<Hypertension>> =
            Observer { observableData ->
                val newAdapter = HypertensionAdapter(this)
                newAdapter.data = observableData
                binding.genoRecyclerview.adapter = newAdapter
                Log.d("Observer", "looking ggod")
            }
        viewmodel.observableData.observe(this, dataObserver)
        setRecyclerView()
        viewmodel.observableData.postValue(receivedData)

        // set textviews
        binding.userNameTextView.text = userName
        binding.userAgeTextView.text = userAge
        binding.userDiseaseTextView.text = userDisease
        binding.userMedicationTextView.text = userMedication
    }


    private fun setRecyclerView() {
        val hypertensionAdapter = HypertensionAdapter(this)
        binding.genoRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.genoRecyclerview.adapter = hypertensionAdapter
        hypertensionAdapter.notifyDataSetChanged()
    }
}
