package com.example.des3776

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.des3776.databinding.ActivityUserInputBinding
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
import java.io.FileOutputStream

class UserInputActivity : AppCompatActivity() {

    /// env
    private lateinit var binding: ActivityUserInputBinding;
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>;

    /// input data
    private var userName = "";
    private var userAge = "";
    private var userDisease = "";
    private var userMedication = "";

    // intent
    private var selectedFileURI: Uri? = null

    // Viewmodel
    private lateinit var viewmodel: HypertensionViewModel


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_input)
        binding.activity = this@UserInputActivity

        // ViewModel
        viewmodel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            HypertensionViewModel::class.java
        )

        val userNameTextView = binding.userNameTextView
        val userAgeTextView = binding.userAgeTextView
        val userDiseaseTextView = binding.userDiseaseTextView
        val userMedicationTextView = binding.userMedicationTextView
        val nextButton = binding.nextButton
        val userNameInputText = binding.userNameInput
        val userAgeInputText = binding.userAgeInput
        val userDiseaseInputText = binding.userDiseaseInput
        val userMedicationInputText = binding.userMedicationInput


        intentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            if (res.resultCode == RESULT_OK) {
                selectedFileURI = res.data?.data
                Log.d("onActivityResult", selectedFileURI!!.toString())
                if (selectedFileURI != null) {
                    createRequest()
                }
            }
        }

        // set text views
        userNameTextView.text = "Type Your Name here"
        userAgeTextView.text = "Type Your Age here"
        userDiseaseTextView.text = "Type Your Disease here"
        userMedicationTextView.text = "Type Your Recent Medication here"

        /// set listeners
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (p0 == null)
//                    return
//                // 1. get cursor position : p0 = start + before
//                val initialCursorPosition = start + before
//                //2. get digit count after cursor position : c0
//                val numOfDigitsToRightOfCursor = getNumberOfDigits(beforeText.substring(initialCursorPosition,
//                    beforeText.length))
//                val newAmount = formatAmount(p0.toString())
//                editText.removeTextChangedListener(this)
//                editText.setText(newAmount)
//                //set new cursor position
//                editText.setSelection(getNewCursorPosition(numOfDigitsToRightOfCursor, newAmount))
//                editText.addTextChangedListener(this)

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && !s.toString().equals("")) {
                    when (s.hashCode()) {
                        userNameInputText.text.hashCode() -> {
                            userNameInputText.removeTextChangedListener(this)
                            userNameInputText.text = s
                            userNameInputText.setSelection(s.length)
                            userName = userNameInputText.text.toString()
                            userNameInputText.addTextChangedListener(this)
                        }
                        userAgeInputText.text.hashCode() -> {
                            userAgeInputText.removeTextChangedListener(this)
                            userAgeInputText.text = s
                            userAgeInputText.setSelection(s.length)
                            userAge = userAgeInputText.text.toString()
                            userAgeInputText.addTextChangedListener(this)
                        }
                        userDiseaseInputText.text.hashCode() -> {
                            userDiseaseInputText.removeTextChangedListener(this)
                            userDiseaseInputText.text = s
                            userDiseaseInputText.setSelection(s.length)
                            userDisease = userDiseaseInputText.text.toString()
                            userDiseaseInputText.addTextChangedListener(this)
                        }
                        userMedicationInputText.text.hashCode() -> {
                            userMedicationInputText.removeTextChangedListener(this)
                            userMedicationInputText.text = s
                            userMedicationInputText.setSelection(s.length)
                            userMedication = userMedicationInputText.text.toString()
                            userMedicationInputText.addTextChangedListener(this)
                        }
                    }
                }
            }

        }

        userNameInputText.addTextChangedListener(textWatcher)
        userAgeInputText.addTextChangedListener(textWatcher)
        userDiseaseInputText.addTextChangedListener(textWatcher)
        userMedicationInputText.addTextChangedListener(textWatcher)

        // set button information
        nextButton.text = "Select File And Analyze"
    }


    @SuppressLint("SetTextI18n")
    fun createRequest() {
        /// set Loading
        binding.nextButton.text = "Loading"
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
            @SuppressLint("ShowToast")
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
                    binding.nextButton.text = "Select File and Send"

                    val displayIntent =
                        Intent(applicationContext, DisplayResultActivity::class.java)
                    displayIntent.putExtra("receivedData", viewmodel.observableData.value)

                    // set other informations
                    displayIntent.putExtra("userName", userName)
                    displayIntent.putExtra("userAge", userAge)
                    displayIntent.putExtra("userDisease", userDisease)
                    displayIntent.putExtra("userMedication", userMedication)

                    startActivity(displayIntent)
                } else {
                    Toast.makeText(applicationContext, "Retry", Toast.LENGTH_SHORT)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d("onRequest", "failed $t")
            }
        })
    }

    fun submitUserInfo(view: View) {
        /// send input data to next screen
        /// select file first
        val selectFileIntent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
        selectFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
//        selectFileIntent.type = "text/comma-separated-values"
        selectFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // execute
        intentLauncher.launch(selectFileIntent)
    }
}
