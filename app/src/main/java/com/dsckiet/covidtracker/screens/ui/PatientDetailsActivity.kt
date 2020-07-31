package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityPatientDetailsBinding
import com.dsckiet.covidtracker.model.AssignPatientLevel
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import kotlinx.android.synthetic.main.activity_patient_details.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PatientDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientDetailsBinding
    private lateinit var tokenManager: TokenManager

    @SuppressLint("LogNotTimber", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_patient_details
        )
        tokenManager = TokenManager(this)

        val patientData = intent.extras?.getBundle("patientData")
        val patientId = patientData?.getString("id")
        val caseId = patientData?.getString("caseId")
        binding.patientId.text = caseId?.substring(0, 5) + "..."
        binding.patientName.text = patientData?.getString("name")
        val patientAge = patientData?.getString("age")
        var patientGender = patientData?.getString("gender")
        if (patientGender == "M") patientGender = "Male"
        binding.patientGA.text = "$patientGender | $patientAge years"
        binding.patientPhoneNo.text = patientData?.getString("contact")
        binding.patientDistrict.text = patientData?.getString("district")
        binding.patientAddress.text = patientData?.getString("address")
        binding.labName.text = patientData?.getString("labName")

        val pageToken = patientData?.getString("pageToken")
        if (pageToken == "0") beginPatientDiagnosis(patientId.toString())
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.docProfileCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${binding.patientPhoneNo.text}"))
            startActivity(intent)
        }
        var level = ""
        var isDeclined: Boolean = false
        //radio buttons clicks
        binding.radioGroupDetails.setOnCheckedChangeListener { _, isCheckedID ->
            var isChecked = binding.L1.isChecked || binding.L2.isChecked || binding.L3.isChecked
            if (isChecked) {
                binding.declineToCome.isChecked = false
            }
        }
        //initial checkbox false
        binding.declineToCome.isChecked = false
        //Check box checks
        binding.declineToCome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioGroupDetails.clearCheck()
                isDeclined = true
            } else {
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

                val warning = AlertDialog.Builder(this)
                warning.setTitle(Html.fromHtml("<font color='#008DB9'>Are you sure want to assign patient</font>"))
                    .setIcon(R.drawable.ic_profile)
                    .setPositiveButton("Yes") { dialog, which ->
                        Log.d("pat_id === ", patientId.toString())
                        sendPatientData(level, comments, isDeclined, patientId.toString())
                        dialog.dismiss()
                    }.setNeutralButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                val dialog: AlertDialog = warning.create()
                dialog.show()
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
                            "Patient can not be attended check network connection",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        onBackPressed()
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if(response.code()!=200)
                        {
                            Toast.makeText(
                                this@PatientDetailsActivity,
                                "Patient can not be attended check network connection",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            onBackPressed()
                        }

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

                    @RequiresApi(Build.VERSION_CODES.KITKAT)
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.code() == 200) {
                            showPopupWindow()
                            Handler().postDelayed({
                                onBackPressed()
                            },1500)
                        }


                    }

                }
            )
    }

    private fun showPopupWindow() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.patient_submitted_popup, null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT

            // Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut
            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(rel_layout)
            popupWindow.showAtLocation(
                rel_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

            Handler().postDelayed({
                popupWindow.dismiss()
            }, 2000)

        }
    }
}