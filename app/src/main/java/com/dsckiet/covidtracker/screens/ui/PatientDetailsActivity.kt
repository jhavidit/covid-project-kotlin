package com.dsckiet.covidtracker.screens.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Html
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityPatientDetailsBinding
import com.dsckiet.covidtracker.model.AssignPatient
import com.dsckiet.covidtracker.model.AssignPatientLevel
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_patient_details.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PatientDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientDetailsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var level: String
    private lateinit var comments: String
    private var isDeclined = false

    @SuppressLint("LogNotTimber", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_patient_details
        )
        level=""
        comments=""
        tokenManager = TokenManager(this)

        val patientData = intent.extras?.getBundle("patientData")
        val patientId = patientData?.getString("id")
        val caseId = patientData?.getString("caseId")
        binding.patientId.text = caseId
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                val intent =
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:${binding.patientPhoneNo.text}"))
                startActivity(intent)
            } else {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "You have denied the permission kindly allow call permission from the settings",
                    Snackbar.LENGTH_LONG
                ).setAction("go", View.OnClickListener {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri =
                        Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }).show()

            }
        }

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
            isDeclined = if (isChecked) {
                binding.radioGroupDetails.clearCheck()
                true
            } else {
                false
            }
        }
        //Radio Button Checks


        binding.submitForm.setOnClickListener {

            if (!InternetConnectivity.isNetworkAvailable(this)!!)
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Internet Unavailable",
                    Snackbar.LENGTH_LONG
                ).show()
            else if (!binding.L1.isChecked && !binding.L2.isChecked && !binding.L3.isChecked && !binding.declineToCome.isChecked) {
                Snackbar.make(
                    binding.coordinatorLayout, "Please assign a severity level to the patient !",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {

                val warning = AlertDialog.Builder(this)
                warning.setTitle(Html.fromHtml("<font color='#008DB9'>Are you sure want to assign patient</font>"))
                    .setIcon(R.drawable.ic_profile)
                    .setPositiveButton("Yes") { dialog, which ->
                        Log.d("pat_id === ", patientId.toString())
                        when {
                            binding.L1.isChecked -> level = "l1"
                            binding.L2.isChecked -> level = "l2"
                            binding.L3.isChecked -> level = "l3"
                        }
                        comments = binding.patientComment.text.toString()
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
        "application/json; charset=utf-8".toMediaTypeOrNull()
        PatientsApi.retrofitService.diagnosisBeginRequest(
            token = tokenManager.getAuthToken().toString(),
            patientId = patientId
        )
            .enqueue(
                object : Callback<ResponseModel> {
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Patient can not be attended check network connection",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        onBackPressed()
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.code() != 200) {
                            Snackbar.make(
                                binding.coordinatorLayout,
                                "Patient can not be attended check network connection",
                                Snackbar.LENGTH_LONG
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
                object : Callback<AssignPatient> {
                    override fun onFailure(call: Call<AssignPatient>, t: Throwable) {
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Check your network connection",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    @RequiresApi(Build.VERSION_CODES.KITKAT)
                    override fun onResponse(
                        call: Call<AssignPatient>,
                        response: Response<AssignPatient>
                    ) {
                        if (response.code() == 200) {
                            showPopupWindow()
                            Handler().postDelayed({
                                startActivity(
                                    Intent(
                                        this@PatientDetailsActivity,
                                        MainActivity::class.java
                                    )
                                )
                            }, 1500)
                        } else {
                            Snackbar.make(
                                binding.coordinatorLayout,
                                response.message(),
                                Snackbar.LENGTH_LONG
                            ).show()
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
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
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