package com.example.des3776

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import club.cred.synth.views.SynthButton
import com.bumptech.glide.Glide
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
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var intentLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this@MainActivity

        /// imageview with animated gif
        val imageView: ImageView = findViewById(R.id.dna_imageview)
        Glide.with(this).load(R.drawable.dna2).into(imageView)

        val sendRequestBtn: SynthButton = findViewById(R.id.send_req_btn)
        sendRequestBtn.text = "Insert User Information"
    }


    fun gotoNextScreen(view: View) {
        val goToNextScreen = Intent(applicationContext, UserInputActivity::class.java)
        startActivity(goToNextScreen)
    }
}
