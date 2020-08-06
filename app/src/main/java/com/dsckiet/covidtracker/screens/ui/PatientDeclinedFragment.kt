package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.FragmentPatientDeclinedBinding
import com.dsckiet.covidtracker.model.PatientDetails
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.screens.adapters.PatientDeclinedAdapter
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.google.android.material.snackbar.Snackbar
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDeclinedFragment : Fragment() {

    private lateinit var binding: FragmentPatientDeclinedBinding
    private lateinit var authToken: TokenManager
    private lateinit var tickerView: TickerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_patient_declined, container, false
            )


        tickerView = binding.countDecPatients
        tickerView.animationInterpolator = OvershootInterpolator()
        tickerView.setCharacterLists(TickerUtils.provideNumberList())
        val fontFace = ResourcesCompat.getFont(requireContext(), R.font.sen_bold)
        tickerView.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        tickerView.typeface = fontFace
        tickerView.gravity = Gravity.START
        tickerView.animationDuration = 2000

        binding.openSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animationView.visibility = VISIBLE
        authToken = TokenManager(requireContext())

        if (!InternetConnectivity.isNetworkAvailable(requireContext())!!) {
            offlineCase()
        } else {
            getDeclinedPatientData()
        }
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

    private fun getDeclinedPatientData() {
        val list = ArrayList<PatientDetails>()
        binding.animationView.setAnimation(R.raw.heartbeat_loading)
        binding.animationView.playAnimation()
        binding.animationView.repeatMode

        PatientsApi.retrofitService.getPatientsData(token = authToken.getAuthToken().toString())
            .enqueue(
                object : Callback<ResponseModel> {
                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        offlineCase()
                        tickerView.text = "0"
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Network Problem",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Open Settings") {
                                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                            }.show()
                    }

                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {

                        binding.animationView.visibility = GONE
                        binding.openSettings.visibility = GONE
                        if (response.code() == 200) {
                            val listResponse = response.body()
                            val listData: List<PatientDetails>? = listResponse?.data
                            if (listData != null) {
                                for (data: PatientDetails in listData) {
                                    list.add(
                                        PatientDetails(
                                            data.phoneNo,
                                            data.district,
                                            data.address,
                                            data.name,
                                            data.age,
                                            data.gender,
                                            data.lab,
                                            data.patientId,
                                            data.caseId
                                        )
                                    )
                                }
                                binding.recyclerView.adapter =
                                    PatientDeclinedAdapter(
                                        requireContext(),
                                        list
                                    )
                                binding.recyclerView.layoutManager =
                                    LinearLayoutManager(requireContext())
                                binding.recyclerView.setHasFixedSize(true)
                                tickerView.text = listData.size.toString()
                                if (listData.isEmpty()) {
                                    binding.animationView.setAnimation(R.raw.empty_list)
                                    binding.animationView.visibility = VISIBLE
                                    binding.openSettings.visibility = GONE
                                    Snackbar.make(
                                        binding.coordinatorLayout,
                                        "No patient available",
                                        Snackbar.LENGTH_INDEFINITE
                                    ).show()
                                }

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
                }
            )
    }
}
