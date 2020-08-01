package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityPatientDetailsBinding
import com.dsckiet.covidtracker.model.AssignPatientLevel
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientDetailsBinding
    private lateinit var tokenManager : TokenManager
    @SuppressLint("LogNotTimber", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_patient_details
        )
        tokenManager = TokenManager(this)
        val patientData = intent.extras?.getBundle("patientData")
        val patientId = patientData?.getString("id")
        binding.patientId.text = patientId?.substring(0, 5) + "..."
        binding.patientName.text = patientData?.getString("name")
        val patientAge = patientData?.getString("age")
        var patientGender = patientData?.getString("gender")
        if(patientGender == "M") patientGender = "Male"
        binding.patientGA.text = "$patientGender | $patientAge years"
        binding.patientPhoneNo.text = patientData?.getString("contact")
        binding.patientDistrict.text = patientData?.getString("district")
        binding.patientAddress.text = patientData?.getString("address")
        binding.labName.text = patientData?.getString("labName")

        val pageToken = patientData?.getString("pageToken")
        if(pageToken == "0") beginPatientDiagnosis(patientId.toString())

        binding.docProfileCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${binding.patientPhoneNo.text.toString()}"))
            startActivity(intent)
        }
        var level = ""
        var isDeclined: Boolean = false
        //radio buttons clicks
        binding.radioGroupDetails.setOnCheckedChangeListener { _, isCheckedID ->
            var  isChecked = binding.L1.isChecked || binding.L2.isChecked || binding.L3.isChecked
            if(isChecked) {
                binding.declineToCome.isChecked = false
            }
        }
        //initial checkbox false
        binding.declineToCome.isChecked = false
        //Check box checks
        binding.declineToCome.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.radioGroupDetails.clearCheck()
                isDeclined = true
            }else{
                isDeclined = false
            }
        }
        //Radio Button Checks
        when {
            binding.L1.isChecked -> level = "l1"
            binding.L2.isChecked -> level = "l2"
            binding.L3.isChecked -> level = "l3"
        }
        val comments: String = binding.commentBox.toString()


        binding.submitForm.setOnClickListener {
            if (!binding.L1.isChecked && !binding.L2.isChecked && !binding.L3.isChecked && !binding.declineToCome.isChecked) {
                Toast.makeText(
                    this, "Please assign a severity level to the patient !",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                sendPatientData(level, comments, isDeclined, patientId.toString())
            }
        }
    }

    private fun beginPatientDiagnosis(patientId: String) {
        println("pat id ======== $patientId")
        "application/json; charset=utf-8".toMediaTypeOrNull()
        PatientsApi.retrofitService.diagnosisBeginRequest(
            token = tokenManager.getAuthToken().toString(),
            patientId = patientId
        )
            .enqueue(
                object : Callback<ResponseModel> {
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Toast.makeText(
                            this@PatientDetailsActivity,
                            "error: ${t.message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        Log.i("test", response.raw().message.toString())
                        println("response body = ${response.body()} and response code = ${response.code()}")
                        Toast.makeText(
                            this@PatientDetailsActivity, "response code : ${response.code()}," +
                                    " response msg : ${response.message()}," +
                                    " response body : ${response.body()}", Toast.LENGTH_LONG
                        ).show()
                    }

                }
            )

    }

    private fun sendPatientData(
        level: String,
        comments: String,
        isDeclined: Boolean,
        patientId: String
    ) {
        Log.i("check", "isDeclined = $isDeclined")
        "application/json; charset=utf-8".toMediaTypeOrNull()
        PatientsApi.retrofitService.assignPatientLevel(
            token = tokenManager.getAuthToken().toString(),
            patientId = patientId,
            patientLevel = AssignPatientLevel(level, comments, isDeclined)
        )
            .enqueue(
                object : Callback<ResponseModel> {
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Toast.makeText(
                            this@PatientDetailsActivity,
                            "error: ${t.message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        Toast.makeText(
                            this@PatientDetailsActivity, "response code : ${response.code()}," +
                                    " response msg : ${response.message()}," +
                                    " response body : ${response.body()}", Toast.LENGTH_LONG
                        ).show()
                    }

                }
            )
    }
}