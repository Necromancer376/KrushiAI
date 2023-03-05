package com.example.agrithonapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity() {

    private val urladmin = "https://agrithon-api.loca.lt/"
    private val url = "https://agrithon-backend-production-e144.up.railway.app/"
    private val urlVoice ="https://agrithon.up.railway.app/"
    lateinit var selectedImageUri: Uri
    var diseaseData = ArrayList<ArrayList<String>>()
//    var response: ImageResponse = ImageResponse("", "", "", "")
    private lateinit var response: NewAPIResponse
    private lateinit var responseAdmin: ImageResponse

    private val REQUEST_CODE_SPEECH_INPUT = 1
    private var count = 0
    private var toggleInput: Boolean = false


    private var contract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        selectedImageUri = it!!
        iv_img_selected.setImageURI(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_add_img.setOnClickListener {
            contract.launch("image/*")
            tv_speech_text.text = ""
            toggleInput = false
        }

        btn_add_voice.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@MainActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            iv_img_selected.setImageURI(null)
            toggleInput = true

        }

//        tv_add_img.setOnClickListener {
//            contract.launch("image/*")
//        }

        btn_img.setOnClickListener {
            count = Regex("""(\s+|(\r\n|\r|\n))""").findAll(tv_speech_text.text.trim()).count() + 1
            Log.e("count", count.toString())
            if (count > 10 && toggleInput) {
                uploadVoice()
            }
            else if(this::selectedImageUri.isInitialized && !toggleInput)
                uploadImage()
            else
                if(tv_speech_text.text != "" || tv_speech_text.text != null)
                    Toast.makeText(this@MainActivity, "Please Give More Detail", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(this@MainActivity, "Please Add Image First", Toast.LENGTH_LONG).show()
        }

        setPlantData()
    }

    private fun uploadImage() {
        showProgressDialog("please wait")

        val filesDir = applicationContext.filesDir
        val file = File(filesDir, "img.png")
        val inputStream = contentResolver.openInputStream(selectedImageUri)
        val outputStream = FileOutputStream(file)
        inputStream!!.copyTo(outputStream)

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
        val partAdmin = MultipartBody.Part.createFormData("img", file.name, requestBody)

        val retrofit = Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadService::class.java)

//        val retrofitAdmin = Retrofit.Builder().baseUrl(urladmin)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(UploadService::class.java)

        CoroutineScope(Dispatchers.Main).launch {
//            responseAdmin = retrofitAdmin.uploadImageAdmin(partAdmin)
            response = retrofit.uploadImage(part)
            var intent = Intent(this@MainActivity, ResultActivity::class.java)
            Log.e("res", response.toString())
            var tempRes = getPlantData(response.class_index.toInt())
            intent.putExtra("RESPONSE", tempRes)
            intent.putExtra("IMAGE", selectedImageUri.toString())
            hideProgressDialog()
            startActivity(intent)
        }
    }

    private fun uploadVoice() {
        showProgressDialog("please wait")

        val retrofit = Retrofit.Builder().baseUrl(urlVoice)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadService::class.java)

        val body = mapOf(
            "text" to tv_speech_text.text.toString()
        )

        CoroutineScope(Dispatchers.Main).launch {
//            responseAdmin = retrofitAdmin.uploadImageAdmin(partAdmin)
            response = retrofit.uploadVoice(body)
            var intent = Intent(this@MainActivity, ResultActivity::class.java)
            Log.e("res", response.toString())
            var tempRes = getPlantData(response.class_index.toInt())
            intent.putExtra("RESPONSE", tempRes)
//            intent.putExtra("IMAGE", selectedImageUri.toString())
            hideProgressDialog()
            startActivity(intent)
        }

    }

    private fun setPlantData() {
        var temp = ArrayList<String>()

        temp.add("Grassy Shoots")
        temp.add("Grassy Shoots is a viral disease that affects banana plants. The disease is caused by the Banana Bunchy Top Virus (BBTV) and is characterized by the appearance of numerous small and narrow leaves, giving the plant a \"grassy\" appearance. Infected plants also exhibit stunted growth, produce few or no fruits, and often die.")
        temp.add("1.The disease is characterized by proliferation of vegetative buds from the base of the cane giving rise to crowded bunch of tillers bearing narrow leaves.\n" +
                "2.The tillers bear pale yellow to completely chlorotic leaves.\n" +
                "3.Cane formation rarely takes place in affected clumps and if formed the canes are thin with short internodes.")
        temp.add("https://www.agrifarming.in/pest-and-disease-management-in-sugarcane-causes-symptoms-chemical-and-biological-control#:~:text=Symptoms%201%20Affected%20shoots%20produce%20numerous%20lanky%20tillers,with%20aerial%20roots%20at%20the%20lower%20nodes.%20")
        diseaseData.add(temp)

        temp = ArrayList<String>()
        temp.add("Healthy")
        temp.add("Your crops are healthy. You took good care of it.")
        temp.add("Your crops are healthy. You took good care of it.")
        temp.add("Just take care of it as you usually do.")
        diseaseData.add(temp)

        temp = ArrayList<String>()
        temp.add("Mites")
        temp.add("Mites are a type of arthropod that can cause damage to crops. They feed on plant tissue, which can result in reduced plant growth and yield. Control measures include the use of pesticides and the implementation of integrated pest management strategies.")
        temp.add("Symptoms of mite damage on crops can include yellowing or bronzing of leaves, stippling or speckling of leaves, distorted or curled leaves, and reduced plant growth or yield. In severe cases, mite infestations can cause defoliation or even death of the plant.")
        temp.add("https://www.ontario.ca/page/mite-pests-greenhouse-crops-description-biology-and-management#:~:text=Mite%20pests%20in%20greenhouse%20crops%3A%20description%2C%20biology%20and,contents.%20...%204%20Management%20Strategies%20Spider%20mites%20")
        diseaseData.add(temp)

        temp = ArrayList<String>()
        temp.add("Ring Spot")
        temp.add("Ring Spot disease is a plant viral disease that can affect a variety of crops. Symptoms include circular or ring-shaped spots on the leaves, which can be yellow, brown, or black in color. Control measures include the use of virus-free planting material and the implementation of good cultural practices.")
        temp.add("Symptoms of Ring Spot disease include circular or ring-shaped spots on the leaves of affected plants, which can be yellow, brown, or black in color. These spots can also appear on the stems and fruits of the plant. In severe cases, the plant may exhibit stunted growth or die.")
        temp.add("https://plantix.net/en/library/plant-diseases/200015/ring-spot-virus")
        diseaseData.add(temp)

        Log.e("disease", diseaseData.toString())
    }

    private fun getPlantData(index: Int): ArrayList<String> {
        return diseaseData[index]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {

                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                tv_speech_text.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }
}