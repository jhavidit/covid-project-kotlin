package com.dsckiet.covidtracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dsckiet.covidtracker.databinding.FragmentPatientDetailsBinding

class PatientDetailsFragment : Fragment() {
    private lateinit var binding: FragmentPatientDetailsBinding
    private var context = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_patient_details, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backBtn.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_patientDetailsFragment_to_diagnosisPendingFragment)
        }
        val patientName = arguments?.getString("name")
        val patientAge = arguments?.getString("age")
        val patientId = arguments?.getString("id")
        val contact = arguments?.getString("contact")
        val gender = arguments?.getString("gender")
        val address = arguments?.getString("address")
        val labName = arguments?.getString("labName")
        val genderAge = "$gender | $patientAge"
        binding.patientNameDetails.text = patientName
        binding.patientAddress.text = address
        binding.patientAge.text = genderAge
        binding.patientId.text = patientId
        binding.patientPhoneNo.text = contact
        binding.labName.text = labName

        binding.docProfileCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$contact"))
            startActivity(intent)
        }
    }

}