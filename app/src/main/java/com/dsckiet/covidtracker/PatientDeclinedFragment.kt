package com.dsckiet.covidtracker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.databinding.FragmentPatientDeclinedBinding
import com.dsckiet.covidtracker.model.PatientDetails
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import kotlinx.android.synthetic.main.fragment_patient_declined.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PatientDeclinedFragment : Fragment() {

    private lateinit var binding: FragmentPatientDeclinedBinding
    private lateinit var authToken: TokenManager
    private var list = ArrayList<PatientDetails>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_patient_declined, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authToken = TokenManager(requireContext())
        getDeclinedPatientData()
        binding.recyclerView.adapter = PatientDeclinedAdapter(requireContext(), list)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun getDeclinedPatientData() {
        PatientsApi.retrofitService.getPatientsData(token = authToken.getAuthToken().toString())
            .enqueue(
                object : Callback<ResponseModel> {
                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Log.d("onFailure response","Network request failed with error: ${t.message}")
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        list = response.body()?.data as ArrayList<PatientDetails>
                        println(list)
                        binding.countDecPatients.text = list.size.toString()
                        Log.d("fetch_success","fetch_success, response: ${response.code()}")
                    }
                }
            )
    }
}
