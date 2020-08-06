package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivityProfileUpdateBinding
import com.dsckiet.covidtracker.network.BASE_URL
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileUpdateActivity : AppCompatActivity() {
    private val RESULT_LOAD_IMAGE = 1
    lateinit var binding: ActivityProfileUpdateBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var doctorId: String
    var path: String = ""
    private var NETWORK_METHOD_FLAG = 0 // Initialize network flag for network request

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_profile_update
        )
        val doctorData = intent.extras?.getBundle("doctorDetails")
        binding.updateName.setText(doctorData?.getString("name"))
        binding.updateAbout.setText(doctorData?.getString("about"))
        binding.updateAddress.setText(doctorData?.getString("address"))
        binding.updateContact.setText(doctorData?.getString("contact"))
        binding.updateAge.setText(doctorData?.getString("age"))
        binding.updateId.setText(doctorData?.getString("id"))
        doctorId = doctorData?.getString("doctorId").toString()
        Glide.with(this).load(doctorData?.getString("image"))
            .into(binding.doctorProfilePhoto)
        println("doctor id : $doctorId")
        binding.updateHospital.setText("city hospital")


        // Initialize Token Manager
        tokenManager = TokenManager(this)
        println("token = ${tokenManager.getAuthToken()}")
        // Back Button
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Profile Photo Selection from gallery
        binding.updatePhoto.setOnClickListener {
            NETWORK_METHOD_FLAG = 1
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, RESULT_LOAD_IMAGE)
        }

        binding.btnUpdateProfile.setOnClickListener {
            if (!binding.updateName.text.isNullOrEmpty()
                && !binding.updateId.text.isNullOrEmpty()
                && !binding.updateAge.text.isNullOrEmpty()
                && !binding.updateAbout.text.isNullOrEmpty()
                && !binding.updateContact.text.isNullOrEmpty()
                && !binding.updateAddress.text.isNullOrEmpty()
                && !binding.updateHospital.text.isNullOrEmpty()
            ) {
                // Checks for permissions if not granted
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        1
                    )
                } else {
                    when (NETWORK_METHOD_FLAG) {
                        0 -> updateDetailsWithoutPhoto()
                        1 -> updateDetailsWithPhoto()
                    }
                }
            } else {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Update Fields cannot be left blank",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            val selectedImage: Uri? = data.data
            val filePathColumn =
                arrayOf(MediaStore.Images.Media.DATA)

            val cursor: Cursor? = contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                val picturePath: String = cursor.getString(columnIndex)
                cursor.close()
                path = picturePath
                Log.d("path", picturePath)
                binding.doctorProfilePhoto.setImageURI(selectedImage)
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun updateDetailsWithoutPhoto() {
        // Launch in Coroutine Scope to avoid Network on main thread exception
        CoroutineScope(IO).launch {
            try {
                val client: OkHttpClient = OkHttpClient().newBuilder()
                    .build()
                "text/plain".toMediaTypeOrNull()
                "application/json; charset=utf-8".toMediaTypeOrNull()
                val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("name", binding.updateName.text.toString())
                    .addFormDataPart("empId", binding.updateId.text.toString())
                    .addFormDataPart("age", binding.updateAge.text.toString())
                    .addFormDataPart(
                        "about",
                        binding.updateAbout.text.toString()
                    )
                    .addFormDataPart("contact", binding.updateContact.text.toString())
                    .addFormDataPart("address", binding.updateAddress.text.toString())
                    .addFormDataPart("gender", "Male")
                    .addFormDataPart("hospital", binding.updateHospital.text.toString())
                    .build()

                // Test your client on testing server => https://{test-server-id}.ngrok.io/
                val request: Request = Request.Builder()
                    .url("${BASE_URL}api/v1/doctors/${doctorId.toString()}")
                    .method("PUT", body)
                    .addHeader("x-auth-token", tokenManager.getAuthToken().toString())
                    .build()

                val response: okhttp3.Response = client.newCall(request).execute()
                Log.d("response: ", "code : ${response.code} and body : ${response.body}")
                if (response.code == 200) {
                    Toast.makeText(
                        this@ProfileUpdateActivity, "Profile updated successfully."
                        , Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ProfileUpdateActivity, MainActivity::class.java))

                } else {

                    Snackbar.make(
                        binding.coordinatorLayout, "Something went wrong! Please try again later."
                        , Snackbar.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("okHttp exception", "${e.message}")
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Some error occurred! Please try again later.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun updateDetailsWithPhoto() {
        // Launch in Coroutine Scope to avoid Network on main thread exception
        CoroutineScope(IO).launch {
            try {
                val client: OkHttpClient = OkHttpClient().newBuilder()
                    .build()
                "text/plain".toMediaTypeOrNull()
                "application/json; charset=utf-8".toMediaTypeOrNull()
                val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image", "image-abc.png",
                        File(path).asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                    .addFormDataPart("name", binding.updateName.text.toString())
                    .addFormDataPart("empId", binding.updateId.text.toString())
                    .addFormDataPart("age", binding.updateAge.text.toString())
                    .addFormDataPart(
                        "about",
                        binding.updateAbout.text.toString()
                    )
                    .addFormDataPart("contact", binding.updateContact.text.toString())
                    .addFormDataPart("address", binding.updateAddress.text.toString())
                    .addFormDataPart("gender", "Male")
                    .addFormDataPart("hospital", binding.updateHospital.text.toString())
                    .build()

                // Test your client on testing server => https://{test-server-id}.ngrok.io/
                val request: Request = Request.Builder()
                    .url("${BASE_URL}api/v1/doctors/${doctorId.toString()}")
                    .method("PUT", body)
                    .addHeader("x-auth-token", tokenManager.getAuthToken().toString())
                    .build()

                val response: okhttp3.Response = client.newCall(request).execute()
                Log.d("response: ", "code : ${response.code} and body : ${response.body}")
                if (response.code == 200) {
                    Toast.makeText(
                        this@ProfileUpdateActivity, "Profile updated successfully."
                        , Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ProfileUpdateActivity, MainActivity::class.java))

                } else {

                    Snackbar.make(
                        binding.coordinatorLayout, "Something went wrong! Please try again later."
                        , Snackbar.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("okHttp exception", "${e.message}")
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Some error occurred! Please try again later.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}