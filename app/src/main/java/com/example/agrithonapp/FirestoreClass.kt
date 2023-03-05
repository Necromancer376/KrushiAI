package com.example.agrithonapp

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {
    private val db = FirebaseFirestore.getInstance()

    fun setData(context: Context, data: HashMap<String, Any>) {
        db.collection("predictions")
            .add(data)
            .addOnSuccessListener {
                Log.d("success", it.toString())
            }
            .addOnFailureListener {
                Log.e("failure", it.toString())
            }
    }

    fun setImageData(activity: ResultActivity, imgUri: Uri, imageType: String) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "." +
                    getFileExtension(activity, imgUri)
        )

        sRef.putFile(imgUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.d("Download Image URL ", uri.toString())
                        activity.uploadData(uri.toString())
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {

        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}