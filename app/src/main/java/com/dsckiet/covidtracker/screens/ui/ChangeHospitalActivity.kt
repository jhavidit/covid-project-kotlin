package com.dsckiet.covidtracker.screens.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivityChangeHospitalBinding
import com.dsckiet.covidtracker.model.AvailableHospital
import com.dsckiet.covidtracker.model.HospitalList
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.screens.adapters.HospitalListAdapter
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.dsckiet.covidtracker.utils.logs
import com.google.android.material.snackbar.Snackbar
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeHospitalActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityChangeHospitalBinding
    private lateinit var level: String
    private val availableHospital = ArrayList<AvailableHospital>()
    private lateinit var allocatedHospital: String
    private lateinit var patientId: String
    private lateinit var tickerView: TickerView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_change_hospital
        )
        tokenManager = TokenManager(this)
        val hospitalData = intent.extras?.getBundle("hospitalDetails")
        level = hospitalData?.getString("level").toString()
        allocatedHospital = hospitalData?.getString("allocatedHospital").toString()
        patientId = hospitalData?.getString("patientId").toString()


        binding.animationView.setAnimation(R.raw.heartbeat_loading)
        binding.animationView.visibility = VISIBLE
        logs("level $level")
        logs("allocatedHospital  $allocatedHospital")

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        PatientsApi.retrofitService.getAvailableHospital(
            token = tokenManager.getAuthToken().toString(), level = level
        ).enqueue(object : Callback<HospitalList> {
            override fun onFailure(call: Call<HospitalList>, t: Throwable) {
                binding.animationView.visibility = GONE
                if (!InternetConnectivity.isNetworkAvailable(this@ChangeHospitalActivity)!!) {
                    offlineCase()
                }
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }

            override fun onResponse(
                call: Call<HospitalList>,
                response: Response<HospitalList>
            ) {
                binding.animationView.visibility = GONE
                logs("success ${response.body().toString()}")

                if (response.code() == 200) {
                    if (response.body()?.data != null) {
                        for (i in response.body()!!.data.indices) {
                            if (response.body()!!.data[i].hospitalId != allocatedHospital)
                                availableHospital += AvailableHospital(
                                    response.body()!!.data[i].name,
                                    response.body()!!.data[i].address,
                                    response.body()!!.data[i].hospitalId
                                )
                        }

                        binding.recyclerView.adapter =
                            HospitalListAdapter(
                                this@ChangeHospitalActivity,
                                availableHospital, allocatedHospital, patientId
                            )
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(this@ChangeHospitalActivity)
                        binding.recyclerView.setHasFixedSize(true)

                        tickerView = binding.hospitalCount
                        tickerView.animationInterpolator = OvershootInterpolator()
                        tickerView.setCharacterLists(TickerUtils.provideNumberList())
                        val fontFace =
                            ResourcesCompat.getFont(this@ChangeHospitalActivity, R.font.sen_bold)
                        tickerView.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
                        tickerView.typeface = fontFace
                        tickerView.gravity = Gravity.START
                        tickerView.animationDuration = 2000
                        tickerView.text = availableHospital.size.toString()
                    }
                    if (availableHospital.isEmpty()) {
                        binding.animationView.setAnimation(R.raw.empty_list)
                        binding.animationView.visibility = VISIBLE
                        binding.openSettings.visibility = GONE
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "No Hospital available",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("SUBMIT") {
                            startActivity(
                                Intent(
                                    this@ChangeHospitalActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }.show()
                    }

                }
                else{
                    val jsonObject= JSONObject(response.errorBody()?.string()!!)

                    Snackbar.make(
                        binding.coordinatorLayout,
                        jsonObject.getString("message"),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

    }

    private fun offlineCase() {
        binding.animationView.setAnimation(R.raw.no_internet)
        binding.animationView.visibility = VISIBLE
        binding.openSettings.visibility = VISIBLE
        Snackbar.make(binding.coordinatorLayout, "Network Problem", Snackbar.LENGTH_LONG)
            .setAction("Open Settings") {
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }.show()
    }
}