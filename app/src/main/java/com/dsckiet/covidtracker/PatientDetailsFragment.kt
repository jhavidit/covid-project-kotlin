package com.dsckiet.covidtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.databinding.FragmentPatientDetailsBinding

class PatientDetailsFragment : Fragment() {
    private lateinit var binding : FragmentPatientDetailsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patient_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val patientId = arguments?.getString("id")
//        val patientName = arguments?.getString("name")
//        val patientAge = arguments?.getString("age")
//        binding.patientId.text = patientId.toString()
//        binding.patientName.text = patientName.toString()
//        binding.patientGenderAge.text = "Male" + patientAge
    }
}