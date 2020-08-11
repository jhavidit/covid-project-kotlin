package com.dsckiet.covidtracker.screens.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivityPatientDetailsBinding
import com.dsckiet.covidtracker.model.AssignPatient
import com.dsckiet.covidtracker.model.AssignPatientLevel
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_patient_details.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class PatientDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientDetailsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var level: String
    private lateinit var comments: String
    private var isDeclined = false
    private var allocatedHospital: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //content view
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_patient_details
        )
        //late initialization
        level = ""
        comments = ""
        //initial checkbox unchecked
        binding.declineToCome.isChecked = false
        //setup token manager
        tokenManager = TokenManager(this)
        //getting intent bundles
        val patientData = intent.extras?.getBundle("patientData")
        val patientId = patientData?.getString("id")
        val caseId = patientData?.getString("caseId")
        val patientAge = patientData?.getString("age")
        var patientGender = patientData?.getString("gender")
        if (patientGender == "M") patientGender = "Male"
        val pageToken = patientData?.getString("pageToken")
        if (pageToken == "0") beginPatientDiagnosis(patientId.toString())
        //setting bundles
        binding.patientId.text = caseId
        binding.patientName.text = patientData?.getString("name")
        val patientGA="$patientGender | $patientAge years"
        binding.patientGA.text = patientGA
        binding.patientPhoneNo.text = patientData?.getString("contact")
        binding.patientDistrict.text = patientData?.getString("district")
        binding.patientAddress.text = patientData?.getString("address")
        binding.labName.text = patientData?.getString("labName")
        if (patientData?.getBoolean("isDeclined")!!) {
            isDeclined = true
            binding.declineToCome.isChecked = true
        }

        //handling custom back button
        binding.backBtn.setOnClickListener {
            onBackPressed()
            finish()
        }

        /* handling call button
            -> check permission granted
            ? call
            : snack bar to ask grant permission
          */
        binding.docProfileCard.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent =
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:${binding.patientPhoneNo.text}"))
                startActivity(intent)
            } else {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Kindly allow call permission from setting",
                    Snackbar.LENGTH_LONG
                ).setAction("go") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.show()
            }
        }

        /* handling radio groups
            -> check any radio button is checked
            ? checkbox (if checked) -> unchecked
            : no change
         */
        binding.radioGroupDetails.setOnCheckedChangeListener { _, _ ->
            val isChecked = binding.L1.isChecked || binding.L2.isChecked || binding.L3.isChecked
            if (isChecked) {
                binding.declineToCome.isChecked = false
            }
        }

        /* handling check boxes
            -> check any radio button is checked
            ? radio group (if checked) -> unchecked
            : no change
         */
        binding.declineToCome.setOnCheckedChangeListener { _, isChecked ->
            isDeclined = if (isChecked) {
                binding.radioGroupDetails.clearCheck()
                true
            } else {
                false
            }
        }

        //handling submit button
        binding.submitForm.setOnClickListener {
            /*
            -> If internet is not available
            -> else if any of the radio button or check box is not clicked
            -> else show final alert dialog
             */
            if (!InternetConnectivity.isNetworkAvailable(this)!!)
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Network Problem",
                    Snackbar.LENGTH_LONG
                ).show()
            else if (!binding.L1.isChecked && !binding.L2.isChecked && !binding.L3.isChecked && !binding.declineToCome.isChecked) {
                Snackbar.make(
                    binding.coordinatorLayout, "Please assign a level/status to the patient",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                //dialog box to confirm
                val warning = AlertDialog.Builder(this)
                warning.setTitle("Assign Patient")
                    .setMessage("Are you sure to assign patient?")
                    .setIcon(R.drawable.ic_profile)
                    .setPositiveButton("Yes") { dialog, _ ->
                        when {
                            binding.L1.isChecked -> level = "l1"
                            binding.L2.isChecked -> level = "l2"
                            binding.L3.isChecked -> level = "l3"
                        }
                        comments = binding.patientComment.text.toString()
                        sendPatientData(level, comments, isDeclined, patientId.toString())
                        dialog.dismiss()
                    }.setNeutralButton("No") { dialog, _ ->
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
                        binding.submitForm.visibility = VISIBLE
                        binding.submitFormAnim.visibility = GONE
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Network Problem",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        onBackPressed()
                    }

                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.code() != 200) {
                            binding.submitForm.visibility = VISIBLE
                            binding.submitFormAnim.visibility = GONE
                            Snackbar.make(
                                binding.coordinatorLayout,
                                "Network Problem",
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
        binding.submitForm.visibility = GONE
        binding.submitFormAnim.visibility = VISIBLE
        "application/json; charset=utf-8".toMediaTypeOrNull()
        PatientsApi.retrofitService.assignPatientLevel(
            token = tokenManager.getAuthToken().toString(),
            patientId = patientId,
            patientLevel = AssignPatientLevel(level, comments, isDeclined)
        )
            .enqueue(
                object : Callback<AssignPatient> {
                    override fun onFailure(call: Call<AssignPatient>, t: Throwable) {
                        binding.submitForm.visibility = VISIBLE
                        binding.submitFormAnim.visibility = GONE
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Network Problem",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    override fun onResponse(
                        call: Call<AssignPatient>,
                        response: Response<AssignPatient>
                    ) {
                        if (response.code() == 200) {

                            binding.submitForm.visibility = VISIBLE
                            binding.submitFormAnim.visibility = GONE
                            binding.rlAssignPatient.visibility = GONE
                            binding.rlLevelAssigned.visibility = VISIBLE

                            if (level.isEmpty()) {
                                binding.levelAllocatedText.text = getString(R.string.declinedToCome)
                                binding.patientHospital.visibility = GONE
                                binding.titleHospital.visibility = GONE
                                binding.changeHospitalText.visibility = GONE
                                binding.titleChangeHospital.visibility = GONE
                                showPopupWindow()
                                Handler().postDelayed({
                                    val intent = Intent(
                                        this@PatientDetailsActivity,
                                        MainActivity::class.java
                                    )
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)

                                }, 1500)
                            } else {
                                if (response.body()?.data != null) {
                                    binding.titleHospital.visibility = VISIBLE
                                    binding.patientHospital.visibility = VISIBLE
                                    binding.titleChangeHospital.visibility = VISIBLE
                                    binding.changeHospitalText.visibility = VISIBLE
                                    val hospitalNameAddress =
                                        response.body()?.data?.name + ", " + response.body()?.data?.address
                                    binding.patientHospital.text = hospitalNameAddress

                                    allocatedHospital = response.body()?.data?.hospitalId
                                    binding.levelAllocatedText.text = level.capitalize()
                                    binding.submitForm.setOnClickListener {
                                        showPopupWindow()
                                        Handler().postDelayed({
                                            val intent = Intent(
                                                this@PatientDetailsActivity,
                                                MainActivity::class.java
                                            )
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                        }, 1500)

                                    }
                                    binding.changeHospitalText.setOnClickListener {
                                        val intent = Intent(
                                            this@PatientDetailsActivity,
                                            ChangeHospitalActivity::class.java
                                        )
                                        val bundle = bundleOf(
                                            "level" to level,
                                            "allocatedHospital" to allocatedHospital,
                                            "patientId" to patientId
                                        )
                                        this@PatientDetailsActivity.startActivity(
                                            intent.putExtra(
                                                "hospitalDetails",
                                                bundle
                                            )
                                        )


                                    }
                                } else {
                                    binding.titleHospital.visibility = GONE
                                    binding.patientHospital.visibility = GONE
                                    binding.titleChangeHospital.visibility = GONE
                                    binding.changeHospitalText.visibility = GONE
                                    Toast.makeText(
                                        this@PatientDetailsActivity,
                                        "No hospitals available right now",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showPopupWindow()
                                    Handler().postDelayed({
                                        val intent = Intent(
                                            this@PatientDetailsActivity,
                                            MainActivity::class.java
                                        )
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }, 1500)
                                }
                            }
                        } else {
                            val jsonObject = JSONObject(response.errorBody()?.string()!!)

                            Snackbar.make(
                                binding.coordinatorLayout,
                                jsonObject.getString("message"),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
    }


    fun showPopupWindow() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view =
            inflater.inflate(R.layout.patient_submitted_popup, binding.coordinatorLayout, false)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.END
            popupWindow.exitTransition = slideOut // Finally, show the popup window on app
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(rel_layout)
        }
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