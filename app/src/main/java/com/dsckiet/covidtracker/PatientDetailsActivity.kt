package com.dsckiet.covidtracker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.TokenManager
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_patient_details)
        tokenManager = TokenManager(this)
        val patientData = intent.extras?.getBundle("patientData")
        val patientId = patientData?.getString("id")
        binding.patientId.text = patientId
        binding.patientName.text = patientData?.getString("name")
        val patientAge = patientData?.getString("age")
        var patientGender = patientData?.getString("gender")
        if(patientGender == "M") patientGender = "Male"
        binding.patientGA.text = "$patientGender | $patientAge years"
        binding.patientPhoneNo.text = patientData?.getString("contact")
        binding.patientDistrict.text = patientData?.getString("district")
        binding.patientAddress.text = patientData?.getString("address")
        binding.labName.text = patientData?.getString("labName")
        beginPatientDiagnosis(patientId.toString())

        binding.docProfileCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${binding.patientPhoneNo.text.toString()}"))
            startActivity(intent)
        }

        var level: String = "l1"
        var isDeclined: Boolean = false
        when {
            binding.L2.isChecked -> level = "l2"
            binding.L3.isChecked -> level = "l3"
        }
        val comments: String = binding.commentBox.toString()
        if (binding.declineToCome.isChecked) isDeclined = true
        else if (!binding.declineToCome.isChecked) isDeclined = false

        binding.submitForm.setOnClickListener {
            if (!binding.L1.isChecked && !binding.L2.isChecked && !binding.L3.isChecked) {
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
                        Log.i("test", response.body().toString())
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