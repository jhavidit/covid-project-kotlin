package com.dsckiet.covidtracker.screens.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.FragmentPatientUnassignedBinding
import com.dsckiet.covidtracker.model.PatientDetails
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.screens.adapters.PatientUnassignedAdapter
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientUnassignedFragment : Fragment() {
    private lateinit var binding: FragmentPatientUnassignedBinding
    private lateinit var authToken: TokenManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_patient_unassigned, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animationView.visibility = View.VISIBLE
        authToken = TokenManager(requireContext())

        getUnassignedPatientData()


    }

    private fun getUnassignedPatientData() {
        val list = ArrayList<PatientDetails>()
        PatientsApi.retrofitService.getUnassignedPatientsData(
            token = authToken.getAuthToken().toString()
        )
            .enqueue(
                object : Callback<ResponseModel> {

                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        binding.animationView.visibility = View.GONE

                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Some problem occurred check your network connection or restart the app",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }


                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        binding.animationView.visibility = View.GONE
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
                                        data.age
                                        ,
                                        data.gender,
                                        data.lab,
                                        data.patientId,
                                        data.caseId
                                    )
                                )
                            }
                            binding.recyclerView.adapter =
                                PatientUnassignedAdapter(
                                    requireContext(),
                                    list
                                )
                            binding.recyclerView.layoutManager =
                                LinearLayoutManager(requireContext())
                            binding.recyclerView.setHasFixedSize(true)
                            binding.countDecPatients.text = listData.size.toString()

                            if (listData.isEmpty()) {
                                Snackbar.make(
                                    binding.coordinatorLayout,
                                    "No patient available",
                                    Snackbar.LENGTH_INDEFINITE
                                ).show()
                            }
                        }
                    }
                }
            )
    }
}