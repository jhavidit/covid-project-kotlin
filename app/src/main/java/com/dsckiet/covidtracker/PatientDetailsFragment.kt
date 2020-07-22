package com.dsckiet.covidtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dsckiet.covidtracker.databinding.FragmentPatientDetailsBinding

class PatientDetailsFragment : Fragment() {
    private lateinit var binding : FragmentPatientDetailsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patient_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backBtn.setOnClickListener{
            it.findNavController().navigate(R.id.action_patientDetailsFragment_to_diagnosisPendingFragment)
        }
    }
}