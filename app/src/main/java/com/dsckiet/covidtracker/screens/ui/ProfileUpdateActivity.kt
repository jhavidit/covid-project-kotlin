package com.dsckiet.covidtracker.screens.ui

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.LoginAPI
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityProfileUpdateBinding
import com.dsckiet.covidtracker.model.UpdateProfileBody
import com.dsckiet.covidtracker.model.UpdateProfileResponse
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileUpdateActivity : AppCompatActivity() {
    private val RESULT_LOAD_IMAGE = 1
    lateinit var binding: ActivityProfileUpdateBinding
    private lateinit var tokenManager: TokenManager
    private var doctorId:String?=""
    var path: String = ""
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
        doctorId = doctorData?.getString("doctorId")
        binding.updateHospital.setText("city hospital")

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        tokenManager = TokenManager(this)
        val token = tokenManager.getAuthToken()
        binding.updatePhoto.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, RESULT_LOAD_IMAGE)

        }
        binding.btnUpdateProfile.setOnClickListener {

            val profile = UpdateProfileBody(
                binding.updateAbout.text.toString(),
                binding.updateAddress.text.toString(),
                binding.updateAge.text.toString(),
                binding.updateContact.text.toString(),
                binding.updateId.text.toString(),
                binding.updateHospital.text.toString(),
                binding.updateName.text.toString()

            )

            if(!binding.updateName.text.isNullOrEmpty()&&!binding.updateId.text.isNullOrEmpty()&&!binding.updateAge.text.isNullOrEmpty()&&!binding.updateAbout.text.isNullOrEmpty()&&!binding.updateContact.text.isNullOrEmpty()&&!binding.updateAddress.text.isNullOrEmpty()&&!binding.updateHospital.text.isNullOrEmpty()) {
                if (doctorId != null && token != null) {
                    val callAPi = LoginAPI.retrofitService.updateUserProfile(
                        token,
                        doctorId!!,
                        profile
                    )
                    callAPi.enqueue(object : Callback<UpdateProfileResponse> {
                        override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                            Snackbar.make(
                                binding.coordinatorLayout,
                                "Some error occurred check your network connection or try again later",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }

                        override fun onResponse(
                            call: Call<UpdateProfileResponse>,
                            response: Response<UpdateProfileResponse>
                        ) {
                            if (response.code() == 200) {
                                Toast.makeText(
                                    this@ProfileUpdateActivity,
                                    "Profile Updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@ProfileUpdateActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Snackbar.make(
                                    binding.coordinatorLayout,
                                    "Some error occurred check your network connection or try again later",
                                    Snackbar.LENGTH_INDEFINITE
                                ).show()
                            }


                        }

                    })
                } else {
                    Snackbar.make(
                        binding.coordinatorLayout,
                        "Some error occurred check your network connection or try again later",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
            else
            {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Update Field can not be empty",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RESULT_LOAD_IMAGE && resultCode === Activity.RESULT_OK && null != data) {
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
}