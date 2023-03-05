package com.example.agrithonapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.android.synthetic.main.activity_result.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ResultActivity : BaseActivity() {

    private lateinit var response: ArrayList<String>
    private lateinit var image: Uri
    private var dataMap = HashMap<String, Any>()

    private var conditions = DownloadConditions.Builder().requireWifi().build()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        setupActionBar()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ResultActivity)
        getLastKnownLocation()

        response = intent.getSerializableExtra("RESPONSE") as ArrayList<String>
        try {
            image = Uri.parse(intent.getStringExtra("IMAGE"))
        }catch (e: Exception) {}


        if(this::image.isInitialized) {
            FirestoreClass().setImageData(this@ResultActivity, image, "asked_image")
        }

        try {
            iv_image_result.setImageURI(image)
        }catch (e: Exception) {}

        tv_disease.text = response.get(0)
        tv_description.text = response.get(1)
        tv_symptoms.text = response.get(2)
        tv_source.text = response.get(3)

//        tv_disease.text = response.prediction
//        tv_description.text = response.description
//        tv_symptoms.text = response.symptoms
//        tv_source.text = response.source
    }

    fun uploadData(url: String){
        dataMap.put("prediction", response.get(0))
        dataMap.put("description", response.get(1))
        dataMap.put("symptoms", response.get(2))
        dataMap.put("source", response.get(3))
        dataMap.put("location", userLocation)
        dataMap.put("time", DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now()).toString())
        dataMap.put("image", url)
        FirestoreClass().setData(this@ResultActivity, dataMap)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_result)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }

        toolbar_result.setNavigationOnClickListener{
            onBackPressed()
            finish()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        finish()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    userLocation = location.latitude.toString()
                    userLocation += ","
                    userLocation += location.longitude.toString()
                }
            }
            .addOnFailureListener {
                Log.e("location", it.toString())
            }

    }

    private fun translateText(text: String): String {
        var translated = ""

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()

        Log.e("here", "translator")
        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener {
                        tv_disease.text = it
//                        Log.e("text", translatedText)
//                        translated = it
                    }
                    .addOnFailureListener {
                        Log.e("error", "translate")
                        Toast.makeText(this@ResultActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Log.e("error", "download")
                Toast.makeText(this@ResultActivity, it.message, Toast.LENGTH_SHORT).show()
            }

        return translated
    }
}