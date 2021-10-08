package com.example.des3776

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.des3776.databinding.ActivityMainBinding
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewmodel: HypertensionViewModel
    private var selectedFileURI: Uri? = null
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>

    //    private lateinit var intentLauncher: ActivityResultLauncher<String>
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewmodel = ViewModelProvider(this).get(HypertensionViewModel::class.java)

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

        intentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
//            ActivityResultContracts.GetContent(),
            ActivityResultCallback { res ->
//                selectedFileURI = res
                if (res.resultCode == RESULT_OK) {
                    selectedFileURI = res.data?.data
                    Log.d("onActivityResult", selectedFileURI!!.toString())
                }
            }
        )

        val reqBtn: Button = findViewById(R.id.req_btn)
        reqBtn.text = "Select File and Send"
    }

    private fun setRecyclerView() {
        val hypertensionAdapter = HypertensionAdapter(this)
        binding.genoRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.genoRecyclerview.adapter = hypertensionAdapter
        hypertensionAdapter.notifyDataSetChanged()
    }

    fun getEvaluationOfSelectedTestCase(view: View) {
        /// select file first
        val selectFileIntent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
        selectFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
//        selectFileIntent.type = "text/comma-separated-values"
        selectFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // execute
        intentLauncher.launch(selectFileIntent)

        if (selectedFileURI != null) {
            // convert to inputStream
            val inputStream = contentResolver.openInputStream(selectedFileURI!!)!!
            val newlyWrittenFile =
                File(cacheDir.absolutePath + "/" + selectedFileURI!!.port)

            // write new file with outputStream
            val outputStream = FileOutputStream(newlyWrittenFile)
            val buf = ByteArray(1024)
            var len: Int
            while (true) {
                len = inputStream.read(buf, 0, 1024)
                if (len > 0) {
                    outputStream.write(buf, 0, len)
                } else break
            }

            outputStream.close()

            Log.d("fileNEW", "wrote everything")

            val requestFile = RequestBody.create(
                MediaType.parse(contentResolver.getType(selectedFileURI!!)!!),
                newlyWrittenFile
            )
            val requestBody =
                MultipartBody.Part.createFormData("file", newlyWrittenFile.name, requestFile)

            // execute the request
            // use Retrofit to create request
            val baseURL = "http://10.0.2.2:3000/"
            val retrofit =
                Retrofit.Builder().baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val api = retrofit.create(HyperTensionAPI::class.java)
            val call = api.getResultFromFile(requestBody)

            /// use in asychronous way
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    Log.d("onCall", call.isExecuted.toString() + call.toString())
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
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("onRequest", "failed $t")
                }
            })
        }
    }
}
